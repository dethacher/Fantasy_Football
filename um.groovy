// Author: David Thacher
// Date: 9/12/2021
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
@Grab(group='org.seleniumhq.selenium', module='selenium-api', version='3.11.0')
@Grab(group='org.seleniumhq.selenium', module='selenium-chrome-driver', version='3.8.1')

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

println "Username"
def username = System.console().readLine().toString()
println "Password"
def password = System.console().readPassword().toString()

System.setProperty("webdriver.chrome.driver", new File("").getAbsolutePath() + "/chromedriver");
WebDriver driver = new ChromeDriver();
driver.get("https://fantasy.nfl.com/account/sign-in");
driver.findElement(By.xpath("//*[@id=\"gigya-loginID-60062076330815260\"]")).sendKeys(username);
driver.findElement(By.xpath("//*[@id=\"gigya-password-85118380969228590\"]")).sendKeys(password);
driver.findElement(By.xpath("/html/body/div[3]/div[3]/div/div[1]/div/div/div[2]/div/form/div[1]/div[4]/input")).click();

def depth(team_url) {

	def results = []
	def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
	def slurper = new XmlSlurper(tagsoupParser)
	def url = "https://www.cbssports.com/nfl/teams/" + team_url + "depth-chart/"
	def htmlParser = slurper.parse(url)
	
	try {
		htmlParser.'**'.findAll{ it.@class == 'TableBase-table' }.collect {
			it.tbody.tr.collect { t ->
				def pos = t.td[0].text().trim()
				def names = []
				
				(1..3).each() { num ->
					try {
						def name = t.td[num].span[1].span[0].a[0].text().trim()
						if (name != "")
							names.add(name)
						else {
							name = t.td[num].span[2].span[0].a[0].text().trim()
							if (name != "")
								names.add(name)
						}
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
					try {
						(0..14).each() { x ->
							def name = t.td[num].div[0].div[x].span[1].span[0].a[0].text().trim()
							if (name != "")
								names.add(name)
							else {
								name = t.td[num].div[0].div[x].span[2].span[0].a[0].text().trim()
								if (name != "")
									names.add(name)
							}
						}
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
				}
				results.add( [ Position: pos, Players: names ] )
			}
		}
	}
	catch (Exception e) {
		// Ignore All Exceptions
	}
	
	return results
}

def players(driver) {
	def results = []
	(0..29).each() { num ->
		def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
		def slurper = new XmlSlurper(tagsoupParser)
		def url = "https://fantasy.nfl.com/league/2393954/players?offset=" + ((num * 25) + 1).toString()
		sleep(1000)
		driver.get(url)
		htmlParser = slurper.parseText(driver.getPageSource())
		
		try {
			htmlParser.'**'.find { it.@class == 'tableType-player hasGroups' }.tbody.tr.collect {
				def pos = ""
				def team = ""
				def player = it.td[1].div.a[0].text().trim()
			        if (it.td[1].div.em.text().split('-').length > 1) {
				        pos = it.td[1].div.em.text().split('-')[0].trim()
				      	team = it.td[1].div.em.text().split('-')[1].trim()
			       }
			       else
			       	pos = it.td[1].div.em.text().trim()
			       results.add( [
			       	Player: player,
			       	Team: team,
			       	Pos: pos
			       	] )
			}

		}
		catch (Exception e) {
			// Ignore All Exceptions
		}
	}
	return results
}

def teams = [
	[ URL: "SF/san-francisco-49ers/", TEAM: "49ers" ],
	[ URL: "ATL/atlanta-falcons/", TEAM: "Falacons" ],
	[ URL: "BUF/buffalo-bills/", TEAM: "Biils" ],
	[ URL: "MIA/miami-dolphins/", TEAM: "Dolphins" ],
	[ URL: "NYJ/new-york-jets/", TEAM: "Jets" ],
	[ URL: "NE/new-england-patriots/", TEAM: "Patriots" ],
	[ URL: "BAL/baltimore-ravens/", TEAM: "Ravens" ],
	[ URL: "CIN/cincinnati-bengals/", TEAM: "Bengals" ],
	[ URL: "CLE/cleveland-browns/", TEAM: "Browns" ],
	[ URL: "PIT/pittsburgh-steelers/", TEAM: "Steelers" ],
	[ URL: "HOU/houston-texans/", TEAM: "Texans" ],
	[ URL: "IND/indianapolis-colts/", TEAM: "Colts" ],
	[ URL: "JAC/jacksonville-jaguars/", TEAM: "Jaguars" ],
	[ URL: "TEN/tennessee-titans/", TEAM: "Titans" ],
	[ URL: "DEN/denver-broncos/", TEAM: "Broncos" ],
	[ URL: "KC/kansas-city-chiefs/", TEAM: "Chiefs" ],
	[ URL: "LAC/los-angeles-chargers/", TEAM: "Chargers" ],
	[ URL: "LV/las-vegas-raiders/", TEAM: "Raiders" ],
	[ URL: "SEA/seattle-seahawks/", TEAM: "Seahawks" ],
	[ URL: "LAR/los-angeles-rams/", TEAM: "Rams" ],
	[ URL: "ARI/arizona-cardinals/", TEAM: "Cardinals" ],
	[ URL: "TB/tampa-bay-buccaneers/", TEAM: "Buccaneers" ],
	[ URL: "NO/new-orleans-saints/", TEAM: "Saints" ],
	[ URL: "CAR/carolina-panthers/", TEAM: "Panthers" ],
	[ URL: "MIN/minnesota-vikings/", TEAM: "Vikings" ],
	[ URL: "GB/green-bay-packers/", TEAM: "Packers" ],
	[ URL: "DET/detroit-lions/", TEAM: "Lions" ],
	[ URL: "CHI/chicago-bears/", TEAM: "Bears" ],
	[ URL: "WAS/washington-football-team/", TEAM: "Washington" ],
	[ URL: "PHI/philadelphia-eagles/", TEAM: "Eagles" ],
	[ URL: "DAL/dallas-cowboys/", TEAM: "Cowboys" ],
	[ URL: "NYG/new-york-giants/", TEAM: "Gaints" ]
]

def pos_lut = [
	[ "Quarterback", "QB" ],
	[ "Running Back", "RB" ],
	[ "Fullback", "FB" ],
	[ "Wide Receiver", "WR" ],
	[ "Tight End", "TE" ],
	[ "Left Tackle", "LT" ],
	[ "Left Guard", "LG" ],
	[ "Center", "C" ],
	[ "Right Guard", "RG" ],
	[ "Right Tackle", "RT" ],
	[ "Left Defensive End", "DL" ],
	[ "Right Defensive End", "DL" ],
	[ "Left Defensive Tackle", "DL" ],
	[ "Nose Tackle", "DL" ],
	[ "Right Defensive Tackle", "DL" ],
	[ "Left Inside Linebacker", "LB" ],
	[ "Right Inside Linebacker", "LB" ],
	[ "Strongside Linebacker", "LB" ],
	[ "Strongside Linebacker", "LB" ],
	[ "Middle Linebacker", "LB" ],
	[ "Weakside Linebacker", "LB" ],
	[ "Right Cornerback", "DB" ],
	[ "Left Cornerback", "DB" ],
	[ "Strong Safety", "DB" ],
	[ "Free Safety", "DB" ],
	[ "Punter", "P" ],
	[ "Kicker", "K" ],
	[ "Long Snapper", "LS" ],
	[ "Holder", "H" ],
	[ "Punt Returner", "PR" ],
	[ "Kick Returner", "KR" ]
]

println "\n\nScript Output"
def table = []

teams.each {
	def team = it.URL.split("/")[0]
	depth(it.URL).each() {
		def num = 1
		def pos = pos_lut.find { p -> p[0] == it.Position }[1]
		it.Players.each() { player ->
			table.add( [
				Player: player,
				Team: team,
				Pos: pos,
				Num: num
				] )
			++num
		}
	}
}

players(driver).each() {
	table.each() { t ->
		if (t.Player == it.Player && t.Pos == it.Pos && t.Team == it.Team)
			if (t.Num <= 3 && t.Pos == "WR")
				println "Found " + t.Pos + "-" + t.Num + " - " + t.Player + " for " + t.Team
	}
}

driver.quit()
