// Author: David Thacher

println "\n\nScript Output"
merger = new src.mailmerge()
fantasy = new src.league()

fantasy.populate(true)

fantasy.teams.each { team ->
	def roster = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "Keeper Cost")
	def broken = sprintf("%s\t%s\t\t%s\t\t%s\n", "Player", "Position", "Penalty", "LTC Contract")
	def money = sprintf("%s\t%s\n", "Budget (includes penalty)", "Penalty")
	def ltc = sprintf("%s\t%s\t\t%s\t\t%s\n", "Player", "Position", "LTC Cost", "LTC Contract")
	def dropped = sprintf("%s\t%s\t\t%s\n", "Player", "Position", "Keeper Cost")
	def roster_total = 0
	def roster_count = 0
	def ltc_total = 0
	def broken_total = 0
	def dropped_total = 0
	def dropped_count = 0
	
	
	fantasy.table.each {
		if (it.Team == team.Team) {
			roster += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Cost)
			roster_total += it.Cost
			++roster_count
		}
	}
	roster += sprintf("\nTotal: (%s Players)\t\t\t\t\$%s\n", roster_count, roster_total)
	
	fantasy.table.each {
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.BROKEN) {
			broken += sprintf("%s\t%s\t\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Penalty, "20" + it.LTC_BEGIN + "-20" + (it.LTC_BEGIN + 2))
			broken_total += it.Penalty
		}
		if (it.LTC_TEAM == team.Team && it.LTC_Status == league.LTC_STATUS.VALID) {
			ltc += sprintf("%s\t%s\t\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.LTC_COST, "20" + it.LTC_BEGIN + "-20" + (it.LTC_BEGIN + 2))
			ltc_total += it.LTC_COST
		}
	}
	broken += sprintf("\nTotal:\t\t\t\$%s\n", broken_total)
	ltc += sprintf("\nTotal:\t\t\t\$%s\n", ltc_total)
	
	fantasy.nonkeepers.each {
		if (it.Team == team.Team) {
			dropped += sprintf("%s\t%s\t\t%s\n", it.Player, it.Position, "\$" + it.Cost)
			dropped_total += it.Cost
			++dropped_count
		}
	}
	dropped += sprintf("\nTotal: (%s Players)\t\t\t\$%s\n", dropped_count, dropped_total)
	
	money +=  sprintf("%s\t%s\n", "\$" + team.Budget, "\$" + team.Penalty)
	
	merger.merge(team.Team, roster, money, ltc, broken, dropped)
}
