public class dansing1MancalaNode extends MancalaNode {

	/**
	 * See corresponding <code>MancalaNode</code> standard constructor documentation.
	 */
	public dansing1MancalaNode() {
		super();
	}

	/**
	 * See corresponding <code>MancalaNode</code> copy constructor documentation.
	 * @param node node to be copied
	 */
	public dansing1MancalaNode(MancalaNode node) {
		super(node);
	}

	/**
	 * See corresponding <code>MancalaNode</code> FairKalah constructor documentation.
	 * @param stateIndex FairKalah initial state index 
	 */
	public dansing1MancalaNode(int stateIndex) {
		super(stateIndex);
	}

	/**
	 * Return the difference between current MAX and MIN score.
	 */
	public double utility() {
		return -0.9780 + (1.0669 * score_dif()) + (1.6337 * relativeMobility()) + (1.6417 * freeMoves()) + (0.7221 * relative_capturable()) + (0.8634 * state[5]);
	}

    public int score_dif() {
        return state[MAX_SCORE_PIT] - state[MIN_SCORE_PIT];
    }
    
    public int relativeMobility() {
        int maxMoves = 0;
        int minMoves = 0;

        for (int i = 0; i < PLAY_PITS; i++) {
            if (state[i] > 0) {
                maxMoves++;
            }
            if (state[i + PLAY_PITS + 1] > 0) {
                minMoves++;
            }
        }

        return maxMoves - minMoves;
    }

    public int freeMoves() {
        int maxMoves = 0;

        for (int i = 0; i < PLAY_PITS; i++) {
            if (state[i] == i) {
                maxMoves++;
            }
        }

        return maxMoves;
    }

	public int relative_capturable() {
		return capturableStones() - vulnerableStones();
	}

	public int vulnerableStones() {
		int vulnerableStones = 0;
		for (int i = PLAY_PITS + 1; i < 2 * PLAY_PITS + 1; i++) {
			int mirroredPit = 2 * PLAY_PITS + 1 - i;
			if (state[i] == 0 && reachablePit(mirroredPit, true)) {
				vulnerableStones += state[PLAY_PITS * 2 - i];
			}
		}
		return vulnerableStones;
	}
	
	public int capturableStones() {
		int capturableStones = 0;
		for (int i = 0; i < PLAY_PITS; i++) {
			if (state[i] == 0 && reachablePit(PLAY_PITS - i, false)) {
				capturableStones += state[PLAY_PITS * 2 - i];
			}
		}
		return capturableStones;
	}
	
	public boolean reachablePit(int pit, boolean opponentSide) {
		int start = opponentSide ? PLAY_PITS + 1 : 0;
		int end = opponentSide ? 2 * PLAY_PITS + 1 : PLAY_PITS;
		int relativePit = opponentSide ? (2 * PLAY_PITS + 1 - pit) : PLAY_PITS - pit;
	
		for (int i = start; i < end; i++) {
			
			if (i == relativePit && state[i] == 13) {
				return true;
			} 
			if (opponentSide && i != relativePit && state[i] == Math.floorMod(relativePit - i, 13)) {
				return true;
			}
			if (!opponentSide && i != relativePit && state[i] == Math.floorMod(PLAY_PITS - i - pit, 13)) {
				return true;
			}
		}

		return false;
	}
}
