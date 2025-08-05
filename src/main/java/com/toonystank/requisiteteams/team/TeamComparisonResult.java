package com.toonystank.requisiteteams.team;

import lombok.Getter;

@Getter
public class TeamComparisonResult {

    private final String teamAName;
    private final String teamBName;
    private final double xpA;
    private final double xpB;
    private final int rankA;
    private final int rankB;

    public TeamComparisonResult(String teamAName, String teamBName, double xpA, double xpB, int rankA, int rankB) {
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.xpA = xpA;
        this.xpB = xpB;
        this.rankA = rankA;
        this.rankB = rankB;
    }

    public int getRankDifference() {
        return Math.abs(rankA - rankB);
    }

    public double getXpDifference() {
        return Math.abs(xpA - xpB);
    }

    public boolean isATeamAhead() {
        return rankA < rankB;
    }

    public String formatComparison() {
        return String.format(
                "%s vs %s\nXP: %.2f vs %.2f\nRank: #%d vs #%d\n→ %s is ahead by %.2f XP and %d rank(s)",
                teamAName,
                teamBName,
                xpA,
                xpB,
                rankA,
                rankB,
                isATeamAhead() ? teamAName : teamBName,
                getXpDifference(),
                getRankDifference()
        );
    }
}
