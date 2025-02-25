Documentation for fairkalah-maia-data.csv
by Todd W. Neller and Taylor C. Neller
http://modelai.gettysburg.edu/2022/fairkalah/
See also http://cs.gettysburg.edu/~tneller/games/fairkalah/ 

Note: The first 14 columns are the piece counts of all play and score pits
starting with the current player's leftmost play pit and proceeding 
counter-clockwise.

                         OPPONENT

	pit_1o   pit_2o   pit_3o   pit_4o   pit_5o   pit_6o

score_o                                               score

    pit_6    pit_5    pit_4    pit_3    pit_2    pit_1 	
	
	                  CURRENT PLAYER

Column name(s) -> description

Inputs:

'pit_6', 'pit_5', 'pit_4', 'pit_3', 'pit_2', 'pit_1' 
-> number of pieces in each pit of the current player from 
   leftmost (6 pits clockwise from the score pit) 
   to rightmost (1 pit clockwise from the score pit)

'score' -> number of pieces in the current player score pit

'pit_6o', 'pit_5o', 'pit_4o', 'pit_3o', 'pit_2o', 'pit_1o'
-> number of pieces in each pit of the current player's opponent's from 
   the opponent's leftmost (6 pits clockwise from the opponent score pit)
   to rightmost (1 pit clockwise from the opponent score pit)

'score_o', -> number of pieces in the current player's opponent's score pit

Outputs (from analysis of the input states):

'optimal_6', 'optimal_5', 'optimal_4', 'optimal_3', 'optimal_2', 'optimal_1'
-> optimal_{n} is 1 or 0 if pit_{n} is or is not an optimal play, respectively

'game_val' -> negamax game value assuming optimal play by both players, i.e.
final current player score minus final opponent player score. Positive, zero,
and negative values indicate winning, tied, losing game values for the 
current player.