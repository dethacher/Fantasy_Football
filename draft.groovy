// Author: David Thacher
// Date: 6/7/2021
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )

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


println "\n\nScript Output"

def file = new File("draft.csv")

draft().each {
	file << it.Player_Name + "," + it.Player_Position + "," + it.Player_Team + "," + it.To + "," + it.Cost.findAll( /\d+/ )*.toInteger()[0] + "\n"
}

