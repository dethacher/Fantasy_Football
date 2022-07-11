// Author: David Thacher
package src
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
import java.util.concurrent.*

def keepers() {
	def results = []
	def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
	def slurper = new XmlSlurper(tagsoupParser)
	def htmlParser = slurper.parse(new File('keepers.html'))

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
	
	return results
}

def keepers_nonfinal(league_id = "2393954") {
	def futures = []
	def executor = Executors.newFixedThreadPool(8)
	
	(0..20).each() { number ->
		futures.add(executor.submit({ -> { num ->
			def results = []
			def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
			def slurper = new XmlSlurper(tagsoupParser)
			def url = "https://fantasy.nfl.com/league/" + league_id + "/team/" + (num).toString()
			def htmlParser = slurper.parse(url)
			def team = ""
			
			try {
				htmlParser.'**'.find { it.@class == 'selecter-selected' }.collect { t ->
					team = t.span[0].text().trim()
				}
			}
			catch (Exception e) {
				// Ignore All Exceptions
			}

			try {
				htmlParser.'**'.find { it.@class == 'tableWrap hasOverlay' }.table.tbody.tr.collect { t ->
					try {
						def player = t.td[2].div[0].a[0].text().trim()
						def pos = ""
						def player_team = ""
						
						if (t.td[2].div.em.text().split('-').length > 1) {
							pos = t.td[2].div.em.text().split('-')[0].trim()
							player_team = t.td[2].div.em.text().split('-')[1].trim()
						}
						else
							pos = t.td[2].div.em.text().split('-')[0].trim()
						
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

			try {
				htmlParser.'**'.find { it.@id == 'tableWrap-K' }.table.tbody.tr.collect { t ->
					try {
						def player = t.td[2].div[0].a[0].text().trim()
						def pos = ""
						def player_team = ""
						
						if (t.td[2].div.em.text().split('-').length > 1) {
							pos = t.td[2].div.em.text().split('-')[0].trim()
							player_team = t.td[2].div.em.text().split('-')[1].trim()
						}
						else
							pos = t.td[2].div.em.text().split('-')[0].trim()
						
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

			try {
				htmlParser.'**'.find { it.@id == 'tableWrap-DP' }.table.tbody.tr.collect { t ->
					try {
						def player = t.td[2].div[0].a[0].text().trim()
						def pos = ""
						def player_team = ""
						
						if (t.td[2].div.em.text().split('-').length > 1) {
							pos = t.td[2].div.em.text().split('-')[0].trim()
							player_team = t.td[2].div.em.text().split('-')[1].trim()
						}
						else
							pos = t.td[2].div.em.text().split('-')[0].trim()
						
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
