// Author: David Thacher
// Date: 9/12/2021
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
@Grab(group='org.seleniumhq.selenium', module='selenium-api', version='3.11.0')
@Grab(group='org.seleniumhq.selenium', module='selenium-chrome-driver', version='3.8.1')

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

System.setProperty("webdriver.chrome.driver", new File("").getAbsolutePath() + "/chromedriver");

if (args.size() > 0 && args[0] == "final") {
	println "Username"
	def username = System.console().readLine().toString()
	println "Password"
	def password = System.console().readPassword().toString()
}

def draft(year = null) {
	def results = []
	def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
	def slurper = new XmlSlurper(tagsoupParser)
	def url = "https://fantasy.nfl.com/league/2393954/draftresults?draftResultsDetail=0&draftResultsTab=nomination&draftResultsType=results"
	if (year != null)
		url = "https://fantasy.nfl.com/league/2393954/history/" + year + "/draftresults?draftResultsDetail=0&draftResultsTab=nomination&draftResultsType=results"
	def htmlParser = slurper.parse(url)

	try {
		results = htmlParser.'**'.find { it.@class == 'results' }.div.ul.li.collect { it ->
			def pos = ""
			def team = ""
			
			switch (it.div.em.text().split('-').length) {
        	        	case 2:
                			pos = it.div.em.text().split('-')[0].trim()
                        		team = it.div.em.text().split('-')[1].trim()
                        		break
                        	case 1:
                        		pos = it.div.em.text().trim()
                        		break
               	}
		
			[
				Player_Name: it.div.a.text().trim(),
				Player_Team: team,
				Player_Position: pos,
				To: it.span[2].a.text().trim(),
				Cost: it.span[3].text().trim()
			]
		}
	}
	catch (Exception e) {
		// Ignore All Exceptions
	}
	
	return results
}

def transaction(year = null) {

	def results = []

	(0..500).each() { num ->
		def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
		def slurper = new XmlSlurper(tagsoupParser)
		def url = "https://fantasy.nfl.com/league/2393954/transactions"
		if (num != 0)
			url = "https://fantasy.nfl.com/league/2393954/transactions?offset=" + ((num * 20) + 1).toString()
		if (year != null)
			if (num == 0)
				url = "https://fantasy.nfl.com/league/2393954/history/" + year + "/transactions"
			else
				url = "https://fantasy.nfl.com/league/2393954/history/" + year + "/transactions?offset=" + ((num * 20) + 1).toString()
		def htmlParser = slurper.parse(url)

		try {
			htmlParser.'**'.find { it.@class == 'tableType-transaction noGroups' }.tbody.tr.collect {
				def pos = ""
			        def team = ""
		       	if (it.td[2].text().trim() == "Trade") {
		       		try {
			       		it.td[3].ul.li.collect { lol ->
			       			pos = ""
			       			team = ""
							if (lol.div.em.text().split('-').length > 1) {
								pos = lol.div.em.text().split('-')[0].trim()
								team = lol.div.em.text().split('-')[1].trim()
					       	}
				       		results.add(
								[
									Action: it.td[2].text().trim(),
									Player_Name: lol.div.a[0].text().trim(),
									Player_Team: team,
									Player_Position: pos,
									From: it.td[4].text().trim(),
									To: it.td[5].text().trim(),
									Cost: 0,
									Date: it.td[0].text().trim(),
									Week: it.td[1].text().trim(),
									Owner: it.td[6].div.span.text().trim()
								]
							)
			       		}
			       	}
			       	catch (Exception e) {
			       		// Ignore All Exceptions
			       	}
		       	}
		       	else {
		       		def to = it.td[5].text().trim()
		       		def cost = 0
		       		if (it.td[4].text().trim() == "Waivers") {
		       			cost = it.td[5].text().trim().split('\\(')[1].findAll( /\d+/ )*.toInteger()[0]
		       			to = it.td[5].text().trim().split('\\(')[0].trim()
		       		}
					if (it.td[3].div.em.text().split('-').length > 1) {
					        pos = it.td[3].div.em.text().split('-')[0].trim()
				        	team = it.td[3].div.em.text().split('-')[1].trim()
			       	}
			       	else
			       		pos = it.td[3].div.em.text().trim()
		       		results.add(
						[
							Action: it.td[2].text().trim(),
							Player_Name: it.td[3].div.a[0].text().trim(),
							Player_Team: team,
							Player_Position: pos,
							From: it.td[4].text().trim(),
							To: to,
							Cost: cost,
							Date: it.td[0].text().trim(),
							Week: it.td[1].text().trim(),
							Owner: it.td[6].div.span.text().trim()
						]
					)
				}
			}
		}
		catch (Exception e) {
			// Ignore All Exceptions
		}
	}
	
	return results
}

def keepers() {
	def results = []
	WebDriver driver = new ChromeDriver();
	driver.get("https://fantasy.nfl.com/account/sign-in");
	driver.findElement(By.xpath("//*[@id=\"gigya-loginID-60062076330815260\"]")).sendKeys(username);
	driver.findElement(By.xpath("//*[@id=\"gigya-password-85118380969228590\"]")).sendKeys(password);
	driver.findElement(By.xpath("/html/body/div[3]/div[3]/div/div[1]/div/div/div[2]/div/form/div[1]/div[4]/input")).click();
	def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
	def slurper = new XmlSlurper(tagsoupParser)
	sleep(1000)
	driver.get("https://fantasy.nfl.com/league/2393954/manage/keeperview")
	htmlParser = slurper.parseText(driver.getPageSource())

	try {
		htmlParser.'**'.find { it.@class == 'tableWrap' }.table.tbody.tr.collect { t ->
			try {
				def team = t.td[0].div[0].a.text().trim()
				def list = t.td[1].text().split("\\d+\\.")
				
				(1..list.length - 1).each { 
					try {
						def pos = ""
						def player_team = ""
						def tmp = list[it].trim().split(',')
						def player = tmp[0].trim()
						
						if (tmp[1].split('-').length > 1) {
							pos = tmp[1].split('-')[0].trim()
							player_team = tmp[1].split('-')[1].trim()
				       	}
				       	else
				       		pos = tmp[1].trim()
						
						results.add([
								Team: team,
								Player: player,
								Player_Team: player_team,
								Position: pos
							])
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
				}
			}
			catch (Exception e) {
				// Ignore All Exceptions
			}
		}
	}
	catch (Exception e) {
		// Ignore All Exceptions
	}
	
	driver.quit()
	
	return results
}

def getMin(position, cost, table) {
	def result = table.get(position)?.value
	cost = Math.round(cost)
	
	if (result > cost)
		return result
	return cost
}

def getName(name, names, flag) {
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


println "\n\nScript Output"

def year = Integer.toString(new Date().year + 1899)
enum LTC_STATUS { NONE, BROKEN, VALID, EXPIRED }
enum STATUS { WAIVERS, KEEPER, TRADED, DROPPED }
def table = []
def teams = []
def WAIVERS_BUDGET = 150
def MAX_BUDGET = 500
def names = []

// Load in Draft Data from last year.
draft(year).each {
	table.add( [
		Player: it.Player_Name,
		Position: it.Player_Position,
		Team: it.To,
		Status: STATUS.KEEPER,
		Cost: it.Cost.findAll( /\d+/ )*.toInteger()[0],
		LTC_TERM: 0,		// Years left
		LTC_Status: LTC_STATUS.NONE,
		LTC_TEAM: "",
		LTC_COST: 0
	])
}

new File("name_changes.csv").eachLine {
	def line = it.split(',')
	
	names.add( [
		To: line[0].trim(),
		From: line[1].trim()
	] )
}

// Apply Old LTCs.
//  CSV uses the following format, one entry per line
//	Player_Name, LTC (xx-yy), Team, Cost
new File("ltc.csv").eachLine {
	def yr = new Date().year - 101
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
				table[index].LTC_TEAM = team
				table[index].LTC_COST = line[3].findAll( /\d+/ )*.toInteger()[0]
				table[index].Cost = table[index].LTC_COST
				table[index].LTC_Status = LTC_STATUS.VALID
				table[index].LTC_TERM = years[1] - yr
			}
		}
	}
}

// Apply All Transactions. (Check for breaks)
transaction(year).reverse().each { record ->
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
					LTC_TERM: 0,
					LTC_Status: LTC_STATUS.NONE,
					LTC_TEAM: "",
					LTC_COST: 0
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
if (args.size() > 0 && args[0] == "final") {
	def tmp = []
	if (false) {
		new File("keepers.csv").eachLine { line ->
			def keeper = line.split(',')
			def team = getName(keeper[1].trim(), names, false)
			def index = table.findIndexValues { it.Player == keeper[0].trim() && it.Team == team }
			
			if (index.size > 1 || index.size == 0)
				System.err.println "Failed to find keeper " + keeper
			else {
				index = (int) index[0]
				tmp.add(table[index])
			}
		}
	}
	else {
		keepers().each { keeper ->
			def team = getName(keeper.Team, names, false)
			def index = table.findIndexValues { it.Player == keeper.Player && it.Team == team }
			
			if (index.size > 1 || index.size == 0)
				System.err.println "Failed to find keeper " + keeper.Player + " on " + team
			else {
				index = (int) index[0]
				table[index].Position = keeper.Position
				tmp.add(table[index])
			}
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

if (args.size() > 0 && args[0] == "final") {
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

println " Roster"
teams.each { team ->
	table.each {
		if (it.Team == team.Team)
			println it
	}
}

println " LTC Broken"
teams.each { team ->
	table.each {
		if (it.LTC_TEAM == team.Team && it.LTC_Status == LTC_STATUS.BROKEN)
			println it
	}
}

println " Money"
teams.each { println it }
