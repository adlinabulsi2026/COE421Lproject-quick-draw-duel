// Prints the scoreboard after each round and the match winner.
public class Scoreboard {

	public void printRound(int round, String result, Player p1, Player p2) {
		System.out.println();
		System.out.println("============ SCOREBOARD ============");
		System.out.println("Round " + round + ": " + result);
		printPlayer(p1);
		printPlayer(p2);
		System.out.println("====================================");
		System.out.println();
	}

	public void printChampion(Player winner, Player p1, Player p2) {
		System.out.println("************************************");
		System.out.println("PLAYER " + winner.getId() + " IS THE FASTEST GUN IN THE WEST!");
		System.out.println("Final score: " + p1.getWins() + " - " + p2.getWins());
		System.out.println("************************************");
		System.out.println();
	}

	private void printPlayer(Player p) {
		System.out.println("Player " + p.getId()
				+ " | Wins: " + p.getWins()
				+ " | Last 5 reactions: " + p.getLastFive() + "ms"
				+ " | Average: " + p.getAverage() + " ms");
	}
}
