// Author: David Thacher
package src
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
import java.util.concurrent.*

class points {
	def populate(league_id = "2393954") {
		def futures = []
		def executor = Executors.newFixedThreadPool(8)
		def pos_list = [ 0, 7, 14 ]
		
		counter.each {
			it.value = 0.0;
		}
		
		pos_list.each() { pos_index ->
			(0..125).each() { number ->
				futures.add(executor.submit({ -> { num ->
					def results = []
					def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
					def slurper = new XmlSlurper(tagsoupParser)
					def url = "https://fantasy.nfl.com/league/" + league_id + "/players?playerStatus=all&position=" + pos_index
					if (num != 0)
						url = "https://fantasy.nfl.com/league/" + league_id + "/players?playerStatus=all&position=" + pos_index + "&offset=" + ((num * 25) + 1).toString()
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
				
					return results
				} number } as Callable))
			}
		}
		
		executor.shutdown()
		
		def results = []
	
		futures.each {
			it.get().each { result ->
				results.add(result)
				def old = counter.get(result.Player_Position)
				counter.remove(result.Player_Position)
				if (old > result.Pts.toDouble())
					counter.put(result.Player_Position, old)
				else
					counter.put(result.Player_Position, result.Pts.toDouble())
			}
		}
		
		println counter

		table = results
	}	
	
	def set_top_earners(players) {
		players.each {
			if (it.Cost.toDouble() > top.get(it.Position).toDouble()) {
				top.remove(it.Position)
				top.put(it.Position, it.Cost.toDouble())
			}
		}
		
		println top
	}
	
	def get_player_cost(player) {
		def index = table.findIndexValues { it.Player_Name == player.Player && it.Player_Position == player.Position }
		
		return Math.round(table[index].Pts[0].toDouble() / counter.get(player.Position).toDouble() * top.get(player.Position).toDouble())
	}
	
	private def counter = [ "QB": 0, "RB": 0, "WR": 0, "TE": 0, "K": 0, "DL": 0, "LB": 0, "DB": 0 ]
	private def top = [ "QB": 0, "RB": 0, "WR": 0, "TE": 0, "K": 0, "DL": 0, "LB": 0, "DB": 0 ]
	private def table = []
}
