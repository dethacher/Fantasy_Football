// Author: David Thacher
// Date: 9/7/2021
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )

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
				def ids = []
				
				(1..3).each() { num ->
					try {
						def name = t.td[num].span[1].span[0].a[0].text().trim()
						String href = t.td[num].span[1].span[0].collect { d -> d.'**'.find { it.name() == 'a' }?.@href }.findAll()[0]
						if (name != "") {
							names.add(name)
							ids.add(href.split("/")[3])
						}
						else {
							name = t.td[num].span[2].span[0].a[0].text().trim()
							href = t.td[num].span[2].span[0].collect { d -> d.'**'.find { it.name() == 'a' }?.@href }.findAll()[0]
							if (name != "") {
								names.add(name)
								ids.add(href.split("/")[3])
							}
						}
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
					try {
						(0..14).each() { x ->
							def name = t.td[num].div[0].div[x].span[1].span[0].a[0].text().trim()
							String href = t.td[num].div[0].div[x].span[1].span[0].collect { d -> d.'**'.find { it.name() == 'a' }?.@href }.findAll()[0]
							if (name != "") {
								names.add(name)
								ids.add(href.split("/")[3])
							}
							else {
								name = t.td[num].div[0].div[x].span[2].span[0].a[0].text().trim()
								href = t.td[num].div[0].div[x].span[2].span[0].collect { d -> d.'**'.find { it.name() == 'a' }?.@href }.findAll()[0]
								if (name != "") {
									names.add(name)
									ids.add(href.split("/")[3])
								}
							}
						}
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
				}
				results.add( [ Position: pos, Players: names, IDs: ids ] )
			}
		}
	}
	catch (Exception e) {
		// Ignore All Exceptions
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

def pos_lut_modified = [
	[ "Quarterback", "QB" ],
	[ "Running Back", "RB" ],
	[ "Fullback", "FB" ],
	[ "Wide Receiver", "WR" ],
	[ "Tight End", "TE" ],
	[ "Left Tackle", "DELETE" ],
	[ "Left Guard", "DELETE" ],
	[ "Center", "DELETE" ],
	[ "Right Guard", "DELETE" ],
	[ "Right Tackle", "DELETE" ],
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
	[ "Punter", "DELETE" ],
	[ "Kicker", "K" ],
	[ "Long Snapper", "DELETE" ],
	[ "Holder", "DELETE" ],
	[ "Punt Returner", "DELETE" ],
	[ "Kick Returner", "DELETE" ]
]

println "\n\nScript Output"
def file = new File(" Depth Chart " + new Date().toTimestamp().toString() + ".csv")
file.text = ''

teams.each {
	def team = it.URL.split("/")[0]

	depth(it.URL).each() {
		def num = 1
		if (true) {
			def pos = pos_lut_modified.find { p -> p[0] == it.Position }[1]
			if (pos != "DELETE") {
				it.Players.eachWithIndex { player, i ->
					file << pos + "-" + Integer.toString(num) + "," + team + "," + player + "," + it.IDs[i] + "\n"
					++num
				}
			}
		}
		else {
			def pos = pos_lut_modified.find { p -> p[0] == it.Position }[1]
			it.Players.eachWithIndex { player, i ->
				file << pos + "-" + Integer.toString(num) + "," + team + "," + player + "," + it.IDs[i] + "\n"
				++num
			}
		}
	}
}
