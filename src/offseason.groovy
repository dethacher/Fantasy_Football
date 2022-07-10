// Author: David Thacher

println "\n\nScript Output"
merger = new mailmerge()
fantasy = new league()

fantasy.populate(true)

fantasy.teams.each { team ->
	def roster = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "Keeper Cost")
	def broken = sprintf("%s\t%s\t\t%s\t\t%s\n", "Player", "Position", "Penalty", "LTC Contract")
	def money = sprintf("%s\t%s\n", "Budget", "Penalty")
	def ltc = sprintf("%s\t%s\t\t%s\t\t%s\n", "Player", "Position", "LTC Cost", "LTC Contract")
	def dropped = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "Keeper Cost")
	
	fantasy.table.each {
		if (it.Team == team.Team) 
			roster += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Cost)
	}
	
	fantasy.table.each {
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.BROKEN)
			broken += sprintf("%s\t%s\t\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Penalty, "20" + it.LTC_BEGIN + "-20" + (it.LTC_BEGIN + 2))
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.VALID)
			ltc += sprintf("%s\t%s\t\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.LTC_COST, "20" + it.LTC_BEGIN + "-20" + (it.LTC_BEGIN + 2))
	}
	
	fantasy.nonkeepers.each {
		if (it.Team == team.Team)
			dropped += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Cost)
	}
	
	money +=  sprintf("%s\t%s\n", "\$" + team.Budget, "\$" + team.Penalty)
	
	merger.merge(team.Team, roster, money, ltc, broken, dropped)
}
