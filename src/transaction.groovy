// Author: David Thacher
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
import java.util.concurrent.*

def transaction(year = null, league_id = "2393954") {
	def futures = []
	def executor = Executors.newFixedThreadPool(8)

	(0..500).each() { number ->
		futures.add(executor.submit({ -> { num ->
			def results = []
			def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
			def slurper = new XmlSlurper(tagsoupParser)
			def url = "https://fantasy.nfl.com/league/" + league_id + "/transactions"
			if (num != 0)
				url = "https://fantasy.nfl.com/league/" + league_id + "/transactions?offset=" + ((num * 20) + 1).toString()
			if (year != null)
				if (num == 0)
					url = "https://fantasy.nfl.com/league/" + league_id + "/history/" + year + "/transactions"
				else
					url = "https://fantasy.nfl.com/league/" + league_id + "/history/" + year + "/transactions?offset=" + ((num * 20) + 1).toString()
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
			
			return results;
		} number } as Callable))
	}
	
	executor.shutdown()
	
	def results = []
	
	futures.each {
		it.get().each { result ->
			results.add(result)
		}
	}
	
	return results
}
