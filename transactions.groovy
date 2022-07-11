// Author: David Thacher
import src.transaction

println "\n\nScript Output"

def file = new File("transactions " + new Date().toTimestamp().toString() + ".csv")
file.text = ''

new transaction().transaction().each {
	if (it.Action != "Lineup" && it.Action != "LM")
		file << it.Date + "," + it.Week + "," + it.Action + "," + it.Player_Name + " " + it.Player_Position + "," + it.From + "," + it.To + "," + it.Owner + "," + it.Player_Position + "," + it.Cost + "," + it.Player_Team + "\n"
}
