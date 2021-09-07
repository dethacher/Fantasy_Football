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
				
				(1..3).each() { num ->
					try {
						def name = t.td[num].span[1].span[0].a[0].text().trim()
						if (name != "")
							names.add(name)
					}
					catch (Exception e) {
						// Ignore All Exceptions
					}
					try {
						(0..56).each() { x ->
							def name = t.td[num].div[0].div[x].span[1].span[0].a[0].text().trim()
							if (name != "")
								names.add(name)
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

def teams = [
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
	[ URL: "SF/san-francisco-49ers/", TEAM: "49ers" ],
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

println "\n\nScript Output"
def dir = new File("CBS_Sports_Depth_Charts")
if (!dir.exists())
	dir.mkdirs()

teams.each {
	def file = new File("CBS_Sports_Depth_Charts/" + it.TEAM + " Depth Chart " + new Date().toTimestamp().toString() + ".csv")
	file.text = ''

	depth(it.URL).each() {
		file << it.Position
		it.Players.each() { player ->
			file << "," + player
		}
		file << "\n"
	}
}
