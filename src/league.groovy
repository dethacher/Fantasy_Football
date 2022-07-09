// Author: David Thacher
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )

class league {
	def populate(isFinal = false) {
		new File("name_changes.csv").eachLine {
			def line = it.split(',')
			
			names.add( [
				To: line[0].trim(),
				From: line[1].trim()
			] )
		}

		// Load in Draft Data from last year.
		drafter.draft(year).each {
			table.add( [
				Player: it.Player_Name,
				Position: it.Player_Position,
				Team: getName(it.To, names, false),
				Status: STATUS.KEEPER,
				Cost: it.Cost.findAll( /\d+/ )*.toInteger()[0],
				Penalty: 0,
				LTC_TERM: 0,		// Years left
				LTC_Status: LTC_STATUS.NONE,
				LTC_TEAM: "",
				LTC_COST: 0,
				LTC_BEGIN: 0
			])
			new_table.add( [
				Player: it.Player_Name,
				Position: it.Player_Position,
				Team: getName(it.To, names, false),
				Status: STATUS.KEEPER,
				Cost: it.Cost.findAll( /\d+/ )*.toInteger()[0],
				Penalty: 0,
				LTC_TERM: 0,		// Years left
				LTC_Status: LTC_STATUS.NONE,
				LTC_TEAM: "",
				LTC_COST: 0,
				LTC_BEGIN: 0
			])
		}

		// Apply Old LTCs.
		//  CSV uses the following format, one entry per line
		//	Player_Name, LTC (xx-yy), Team, Cost
		new File("ltc.csv").eachLine {
			def yr = new Date().year - 100 - 1	// The draft has not occured so still last year
			def line = it.split(',')
			def team = getName(line[2], names, false)
			def index = table.findIndexValues { it.Player == line[0] && it.Team == team }
			
			if (index.size > 1 || index.size == 0)
				System.err.println "Failed to find LTC " + line[0]
			else {
				def years = line[1].findAll( /\d+/ )*.toInteger()
				if (years.size != 2)
					System.err.println "Failed to apply LTC for " + line[0] 
				else {
					if (years[1] - yr > 0) {
						index = (int) index[0]
						if (years[0] > table[index].LTC_BEGIN) {
							table[index].LTC_TEAM = team
							table[index].LTC_COST = line[3].findAll( /\d+/ )*.toInteger()[0]
							table[index].Cost = table[index].LTC_COST
							table[index].LTC_Status = LTC_STATUS.VALID
							table[index].LTC_TERM = years[1] - yr
							table[index].LTC_BEGIN = years[0]
						}
					}
				}
			}
		}

		// Apply All Transactions. (Check for breaks)
		transactions.transaction(year).reverse().each { record ->
			switch(record.Action) {
				case "Add":
					def isFound = false
					table.eachWithIndex { t, i ->
						if (t.Player == record.Player_Name && t.Position == record.Player_Position) {
							isFound = true
							table[i].Cost = record.Cost
							table[i].Team = record.To
							table[i].Status = STATUS.WAIVERS
						}
					}
					if (!isFound) {
						table.add( [
							Player: record.Player_Name,
							Position: record.Player_Position,
							Team: record.To,
							Status: STATUS.WAIVERS,
							Cost: record.Cost,
							Penalty: 0,
							LTC_TERM: 0,
							LTC_Status: LTC_STATUS.NONE,
							LTC_TEAM: "",
							LTC_COST: 0,
							LTC_BEGIN: 0
						])
					}
					break
				case "Drop":
					table.eachWithIndex { t, i ->
						if (t.Player == record.Player_Name && t.Position == record.Player_Position) {
							table[i].Team = ""
							table[i].Status = STATUS.DROPPED
							if (table[i].LTC_Status == LTC_STATUS.VALID)
								table[i].LTC_Status = LTC_STATUS.BROKEN
						}
					}
					break
				case "Trade":
					table.eachWithIndex { t, i ->
						if (t.Player == record.Player_Name && t.Position == record.Player_Position) {
							table[i].Team = record.To
							if (table[i].LTC_Status == LTC_STATUS.VALID)
								table[i].LTC_Status = LTC_STATUS.NONE
							table[i].Status = STATUS.TRADED
						}
					}
					break
			}
		}

		// Update with Keepers. (Check for breaks)
		if (isFinal) {
			def tmp = []
			
			keeper.keepers_nonfinal().each { keeper ->
				def team = getName(keeper.Team, names, false)
				def index = table.findIndexValues { it.Player == keeper.Player && it.Team == team }
				
				if (index.size > 1 || index.size == 0)
					System.err.println "Failed to find keeper " + keeper.Player + " on " + team
				else {
					index = (int) index[0]
					table[index].Position = keeper.Position
					table[index].Status = STATUS.KEEPER
					tmp.add(table[index])
				}
			}

			table.eachWithIndex { it, i ->
				if (!tmp.contains(table[i])) {
					switch (it.LTC_Status) {
						case LTC_STATUS.VALID:
						case LTC_STATUS.BROKEN:
							table[i].Team = ""
							table[i].Status = STATUS.DROPPED
							table[i].LTC_Status = LTC_STATUS.BROKEN
							tmp.add(table[i])
							break
					}
				}
			}

			table = tmp
		}

		// Find total cost and penalities
		table.eachWithIndex { it, i ->
			def draft = [ "QB":15, "RB":10, "WR":10, "TE":6, "K":1, "DL":5, "LB":8, "DB":8, "":0 ]	
			def trade = [ "QB":20, "RB":13, "WR":13, "TE":8, "K":1, "DL":7, "LB":11, "DB":11, "":0 ]
			
			switch (table[i].Status) {
				case STATUS.TRADED:
					table[i].Cost = getMin(it.Position, table[i].Cost * 0.7, trade)
					break;
				case STATUS.WAIVERS:
					table[i].Cost = getMin(it.Position, table[i].Cost * 1.2, draft)
					break;
				case STATUS.KEEPER:
					if (table[i].LTC_Status != LTC_STATUS.VALID)
						table[i].Cost = getMin(it.Position, table[i].Cost * 1.15, draft)
					else
						table[i].Cost = getMin(it.Position, table[i].Cost, draft)
					break;
			}
			
			if (table[i].LTC_Status == LTC_STATUS.BROKEN) {
				def pen = 0	
				def y1 = table[i].LTC_COST
				def y2 = Math.round(table[i].LTC_COST * 1.15)
				def y3 = Math.round(y2 * 1.15)
				
				switch (table[i].LTC_TERM) {
					case 2:
						pen = (y3 - y1) + (y2 - y1)
						break
					case 1:
						pen = y3 - y1
						break
				}
				
				pen = Math.round(pen)
				
				def isFound = false
				teams.eachWithIndex { team, x ->
					if (team.Team == it.LTC_TEAM) {
						isFound = true
						teams[x].Penalty += pen
						teams[x].Budget -= pen
						if (teams[x].Budget < WAIVERS_BUDGET)
							teams[x].Waivers = teams[x].Budget
					}
				}
				
				if (!isFound && it.LTC_TEAM != "") {
					def wav = MAX_BUDGET - pen
					if ((MAX_BUDGET - pen) > WAIVERS_BUDGET)
						wav = WAIVERS_BUDGET
					teams.add([
						Team: it.LTC_TEAM,
						Penalty: pen,
						Budget: (MAX_BUDGET - pen),
						Waivers: wav
					])
				}
				table[i].Penalty = pen
			}
			
			if (table[i].Status != STATUS.DROPPED) {
				def isFound = false
				teams.eachWithIndex { team, x ->
					if (team.Team == it.Team) {
						isFound = true
						teams[x].Budget -= table[i].Cost
						if (teams[x].Budget < WAIVERS_BUDGET)
							teams[x].Waivers = teams[x].Budget
					}
				}
				
				if (!isFound && it.Team != "") {
					def wav = MAX_BUDGET - table[i].Cost
					if ((MAX_BUDGET - table[i].Cost) > WAIVERS_BUDGET)
						wav = WAIVERS_BUDGET
					teams.add([
						Team: it.Team,
						Penalty: 0,
						Budget: (MAX_BUDGET - table[i].Cost),
						Waivers: wav
					])
				}
			}
		}

		if (isFinal) {
			teams.eachWithIndex { team, x ->
				while (teams[x].Budget < 25) {
					def cost = 0
					def pen = 0
					def player
					table.each {
						if (it.Team == team.Team) {
							if (it.Cost > cost) {
								player = it
								cost = it.Cost
							}
						}
					}
					table.eachWithIndex { p, i ->
						if (p == player) {
							table[i].Status = STATUS.DROPPED
							table[i].Team = ""	
							if (table[i].LTC_Status == LTC_STATUS.VALID) {	
								def y1 = table[i].LTC_COST
								def y2 = Math.round(table[i].LTC_COST * 1.15)
								def y3 = Math.round(y2 * 1.15)
								
								switch (table[i].LTC_TERM) {
									case 2:
										pen = (y3 - y1) + (y2 - y1)
										break
									case 1:
										pen = y3 - y1
										break
								}
								
								pen = Math.round(pen)
								teams[x].Penalty += pen
								teams[x].Budget -= pen
								if (teams[x].Budget < WAIVERS_BUDGET)
									teams[x].Waivers = teams[x].Budget
								table[i].LTC_Status = LTC_STATUS.BROKEN
								table[i].Penalty = pen
							}
						}
					}
					teams[x].Budget += cost
					if (teams[x].Budget < WAIVERS_BUDGET)
						teams[x].Waivers = teams[x].Budget
					println team.Team + " forced to drop " + player.Player + " causing penalty " + pen
				}
			}
		}

		table.eachWithIndex { it, i ->
			table[i].Team = getName(table[i].Team, names, true)
			table[i].LTC_TEAM = getName(table[i].LTC_TEAM, names, true)
		}

		teams.eachWithIndex { it, i ->
			teams[i].Team = getName(teams[i].Team, names, true)
		}
	}
	
	private def getMin(position, cost, table) {
		def result = table.get(position)?.value
		cost = Math.round(cost)
		
		if (result > cost)
			return result
		return cost
	}

	// If flag is true promote the name to new name, otherwise demote it to old name
	private def getName(name, names, flag) {
		def result = name.trim()
		names.each {
			if (flag) {
				if (it.From == name.trim())
					result = it.To
			}
			else {
				if (it.To == name.trim())
					result = it.From
			}
		}
		return result
	}
	
	
	enum LTC_STATUS { NONE, BROKEN, VALID, EXPIRED }
	enum STATUS { WAIVERS, KEEPER, TRADED, DROPPED }
	def teams = []
	def table = []

	private def year = Integer.toString(new Date().year + 1900 - 1)	// Last Year's draft year
	private def new_table = []
	private def WAIVERS_BUDGET = 150
	private def MAX_BUDGET = 500
	private def names = []
	
	private def drafter = new draft()
	private def keeper = new keepers()
	private def transactions = new transaction()
}
