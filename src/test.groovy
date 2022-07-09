// Author: David Thacher
import groovy.json.JsonBuilder

println "\n\nScript Output"
merger = new mailmerge()
fantasy = new league()

// 2393954

fantasy.populate(args.size() > 0 && args[0] == "final")

fantasy.teams.each { team ->
	def roster = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "Cost")
	def broken = sprintf("%s\t%s\t\t%s\t\t%s\n", "Player", "Position", "Penalty", "LTC Start")
	def money = sprintf("%s\t%s\t\t%s\n", "Budget", "Penalty", "Waivers")
	def ltc = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "LTC Cost")
	
	fantasy.table.each {
		if (it.Team == team.Team) 
			roster += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, it.Cost)
	}
	
	fantasy.table.each {
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.BROKEN)
			broken += sprintf("%s\t%s\t\t%s\t\t%s\n", it.Player, it.Position, it.Penalty, "20" + it.LTC_BEGIN)
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.VALID)
			ltc += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, it.LTC_COST)
	}
	
	money +=  sprintf("%s\t%s\t\t%s\n", team.Budget, team.Penalty, team.Waivers)
	
	merger.merge(team.Team, roster, money, ltc, broken)
}
