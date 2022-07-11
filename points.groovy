// Author: David Thacher
// Date: 10/31/2021
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )

def points() {
	def results = []
	def pos_list = [ 0, 7, 14 ]
	
	pos_list.each() { pos_index ->
		(0..125).each() { num ->
			def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
			def slurper = new XmlSlurper(tagsoupParser)
			def url = "https://fantasy.nfl.com/league/2393954/players?playerStatus=all&position=" + pos_index
			if (num != 0)
				url = "https://fantasy.nfl.com/league/2393954/players?playerStatus=all&position=" + pos_index + "&offset=" + ((num * 25) + 1).toString()
			def htmlParser = slurper.parse(url)

			try {
				htmlParser.'**'.find { it.@class == 'tableType-player hasGroups' }.tbody.tr.collect {
					def pos = ""
					def team = ""
					String href = it.td[1].div.collect { d -> d.'**'.find { it.name() == 'a' }?.@href }.findAll()[0]
					
					if (it.td[1].div.em.text().split('-').length > 1) {
						pos = it.td[1].div.em.text().split('-')[0].trim()
						team = it.td[1].div.em.text().split('-')[1].trim()
					}
					else
						pos = it.td[1].div.em.text().trim()
					
					def points = it.td[20].span.text().trim()
					if (pos_index == 7)
						points = it.td[16].span.text().trim()
					else if (pos_index == 14)
						points = it.td[22].span.text().trim()

					results.add(
						[
							Player_Name: it.td[1].div.a[0].text().trim(),
							Player_Team: team,
							Player_Position: pos,
							Player_Id: href.split('=')[2],
							Pts: points
						]
					)
				}
			}
			catch (Exception e) {
				// Ignore All Exceptions
			}
		}
	}

	return results
}

println "\n\nScript Output"

def file = new File("points " + new Date().toTimestamp().toString() + ".csv")
file.text = ''

points().each {
	file << it.Player_Name + "," + it.Player_Team + "," + it.Player_Position + "," + it.Player_Id + "," + it.Pts + "\n"
}
