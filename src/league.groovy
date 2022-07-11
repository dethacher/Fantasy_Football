// Author: David Thacher
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )

class league {
	def populate(isFinal = false) {
		// Load name changes from name_changes.csv
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
		
		table.eachWithIndex { it, i ->
			def isFound = false
			
			teams.eachWithIndex { team, x ->
				if (team.Team == it.Team)
					isFound = true
			}
			
			if (!isFound && it.Team != "") {
				teams.add([
					Team: it.Team,
					Penalty: 0,
					Budget: MAX_BUDGET,
					Waivers: WAIVERS_BUDGET
				])
			}
		}

		// Update with Keepers. (Check for breaks)
		if (isFinal) {
			def tmp = []
			
			// Populate Keepers
			keeper.keepers_nonfinal().each { keeper ->
				def team = getName(keeper.Team, names, false)
				def index = table.findIndexValues { it.Player == keeper.Player && it.Team == team }
				
				if (index.size > 1 || index.size == 0) {
					if (keeper.Player != "")
						System.err.println "Failed to find keeper " + keeper.Player + " on " + team
				}
				else {
					// Build two tables to so that they use the same references
					index = (int) index[0]
					table[index].Position = keeper.Position
					tmp.add(table[index])
				}
			}

			// Check LTC Status
			table.eachWithIndex { it, i ->
				if (!tmp.contains(table[i])) {
					// Add players being dropped before draft to nonkeepers list
					//	Make a deep copy
					nonkeepers.add([
						Player: table[i].Player,
						Position: table[i].Position,
						Team: table[i].Team,
						Status: table[i].Status,
						Cost: table[i].Cost,
						Penalty: table[i].Penalty,
						LTC_TERM: table[i].LTC_TERM,
						LTC_Status: table[i].LTC_Status,
						LTC_TEAM: table[i].LTC_TEAM,
						LTC_COST: table[i].LTC_COST,
						LTC_BEGIN: table[i].LTC_BEGIN
					])
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
			
			// Compute Keeper Costs
			compute_keeper_cost(table[i])
			
			// Compute LTC Penalties
			if (table[i].LTC_Status == LTC_STATUS.BROKEN) {
				def pen = compute_penalty(table[i].LTC_COST, table[i].LTC_TERM)					
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
			
			// Compute Salary Cap
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
		
		// Compute nonkeepers cost
		nonkeepers.eachWithIndex { it, i ->
			compute_keeper_cost(nonkeepers[i])
		}

		if (isFinal) {
			// Check Salary Cap
			teams.eachWithIndex { team, x ->
				while (teams[x].Budget < 25) {
					def cost = 0
					def pen = 0
					def player
					
					// Find the most expensive player
					table.each {
						if (it.Team == team.Team) {
							if (it.Cost > cost) {
								player = it
								cost = it.Cost
							}
						}
					}
					
					// Drop them
					table.eachWithIndex { p, i ->
						if (p == player) {
							table[i].Status = STATUS.DROPPED
							// Make a deep copy
							nonkeepers.add([
								Player: player.Player,
								Position: player.Position,
								Team: player.Team,
								Status: player.Status,
								Cost: player.Cost,
								Penalty: player.Penalty,
								LTC_TERM: player.LTC_TERM,
								LTC_Status: player.LTC_Status,
								LTC_TEAM: player.LTC_TEAM,
								LTC_COST: player.LTC_COST,
								LTC_BEGIN: player.LTC_BEGIN
							])
							table[i].Team = ""	
							if (table[i].LTC_Status == LTC_STATUS.VALID) {	
								pen = compute_penalty(table[i].LTC_COST, table[i].LTC_TERM)
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
					
					println getName(team.Team, names, true) + " forced to drop " + player.Player + " causing penalty " + pen
				}
			}
		}

		// Correct the Team Names
		table.eachWithIndex { it, i ->
			table[i].Team = getName(table[i].Team, names, true)
			table[i].LTC_TEAM = getName(table[i].LTC_TEAM, names, true)
		}
		
		teams.eachWithIndex { it, i ->
			teams[i].Team = getName(teams[i].Team, names, true)
		}
		
		nonkeepers.eachWithIndex { it, i ->
			nonkeepers[i].Team = getName(nonkeepers[i].Team, names, true)
		}
	}
	
	private def compute_penalty(cost, ltc_term) {
		def y1 = cost
		def y2 = Math.round(cost * 1.15)
		def y3 = Math.round(y2 * 1.15)
		def pen = 0
								
		switch (ltc_term) {
			case 2:
				pen = (y3 - y1) + (y2 - y1)
				break
			case 1:
				pen = y3 - y1
				break
		}
								
		return Math.round(pen)
	}
	
	private def compute_keeper_cost(player) {
		switch ((STATUS) player.Status) {
			case STATUS.TRADED:
				player.Cost = getMin(player.Position, Math.round(player.Cost * 0.7), trade)
				break;
			case STATUS.WAIVERS:
				player.Cost = getMin(player.Position, Math.round(player.Cost * 1.2), draft)
				break;
			case STATUS.KEEPER:
				if (player.LTC_Status != LTC_STATUS.VALID)
					player.Cost = getMin(player.Position, Math.round(player.Cost * 1.15), draft)
				else
					player.Cost = getMin(player.Position, player.Cost, draft)
				break;
		}
	}
	
	private def getMin(position, cost, table) {
		def result = table.get(position)?.value
		
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
	def nonkeepers = []

	private def year = Integer.toString(new Date().year + 1900 - 1)	// Last Year's draft year
	private def new_table = []
	private def WAIVERS_BUDGET = 150
	private def MAX_BUDGET = 500
	private def names = []
	
	private def draft = [ "QB":15, "RB":10, "WR":10, "TE":6, "K":1, "DL":5, "LB":8, "DB":8, "":0 ]	
	private def trade = [ "QB":20, "RB":13, "WR":13, "TE":8, "K":1, "DL":7, "LB":11, "DB":11, "":0 ]
	
	private def drafter = new draft()
	private def keeper = new keepers()
	private def transactions = new transaction()
}
