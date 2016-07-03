import java.util.ArrayList;

public class Solver {

	public static class SolverException extends Exception {
		public SolverException(String message) {
			super(message);
		}
	}
	
	public static final Cube.Color WHITE = Cube.Color.WHITE;
	public static final Cube.Color YELLOW = Cube.Color.YELLOW;
	public static final Cube.Color RED = Cube.Color.RED;
	public static final Cube.Color GREEN = Cube.Color.GREEN;
	public static final Cube.Color BLUE = Cube.Color.BLUE;
	public static final Cube.Color ORANGE = Cube.Color.ORANGE;

	public static final String LOG_START = "START";
	public static final String LOG_GET_WHITE_CROSS = "1_1";
	public static final String LOG_FIX_WHITE_CROSS = "1_2";
	public static final String LOG_GET_WHITE_CORNERS = "1_3";
	public static final String LOG_GET_SECOND_LAYER = "2_1";
	public static final String LOG_GET_YELLOW_CROSS = "3_1";
	public static final String LOG_FIX_YELLOW_CROSS = "3_2";
	public static final String LOG_POSITION_YELLOW_CORNERS = "3_3";
	public static final String LOG_FIX_YELLOW_CORNERS = "3_4";
	public static final String LOG_END = "END";

	private Cube cube;

	public Solver(Cube cube) {
		this.cube = cube;
	}

	public boolean solve() {
		try {
			cube.log(LOG_START);
			cube.log(LOG_GET_WHITE_CROSS);
			getWhiteCross();
			cube.log(LOG_FIX_WHITE_CROSS);
			fixWhiteCross();
			cube.log(LOG_GET_WHITE_CORNERS);
			getWhiteCorners();
			cube.log(LOG_GET_SECOND_LAYER);
			getSecondLayer();
			cube.log(LOG_GET_YELLOW_CROSS);
			getYellowCross();
			cube.log(LOG_FIX_YELLOW_CROSS);
			fixYellowCross();
			cube.log(LOG_POSITION_YELLOW_CORNERS);
			positionYellowCorners();
			cube.log(LOG_FIX_YELLOW_CORNERS);
			fixYellowCorners();
			cube.log(LOG_END);
			return isSolved();
		} catch(SolverException e) {
			return false;
		}		
	}

	private Cube.Spot[] edges(Cube.Color c) {
		Cube.Spot[] edges = new Cube.Spot[4];
		for(int i = 0; i < 4; i++) {
			edges[i] = new Cube.Spot(c, cube.faces.get(c).adjs[i]);
		}
		return edges;
	}

	private Cube.Spot[] corners(Cube.Color c) {
		Cube.Spot[] corners = new Cube.Spot[4];
		for(int i = 0; i < 4; i++) {
			corners[i] = new Cube.Spot(c, cube.faces.get(c).adjs[i], cube.faces.get(c).adjs[(i+1) % 4]);
		}
		return corners;
	}

	public boolean isFixed(Cube.Piece p) {
		Cube.Tile t = p.getTiles()[0];
		return (p == cube.at(p.colors()) && t.facing(t.color));
	}

	public int numFixed(Cube.Color c, Cube.Type type) {
		int num = 0;
		Cube.Spot[] spots = (type == Cube.Type.EDGE) ? edges(c) : corners(c);
		Cube.Piece p;
		for(Cube.Spot spot : spots) {
			if(isFixed(cube.pieces.get(spot))) num++;
		}
		return num;
	}

	public int numPositioned(Cube.Color c, Cube.Type type) {
		int num = 0;
		Cube.Spot[] spots = (type == Cube.Type.EDGE) ? edges(c) : corners(c);
		Cube.Piece p;
		for(Cube.Spot spot : spots) {
			if(cube.at(spot).colors().equals(spot)) num++;
		}
		return num;
	}

	public boolean checkCross(Cube.Color c, boolean fixed) {
		Cube.Piece p;
		for(Cube.Spot spot : edges(c)) {
			p = cube.pieces.get(spot);
			if(!fixed && !p.getTile(c).facing(c)) return false;
			if(fixed) {
				for(Cube.Tile t : p.getTiles()) {
					if(!t.facing(t.color)) return false;
				}
			}
		}
		return true;
	}

	public boolean isSolved() {
		return (checkFirstLayer() && checkSecondLayer() && checkThirdLayer());
	}
	
	/** FIRST LAYER **/

	public void getWhiteCross() {
		for(Cube.Spot edge : edges(WHITE)) {
			Cube.Piece pTarget = cube.pieces.get(edge);
			Cube.Color sideColor, whiteSideColor, otherSideColor;
			switch(pTarget.layer()) {
				case 1:
					if(pTarget.getTile(WHITE).facing(WHITE)) {
						// do nothing
					} else {
						sideColor = pTarget.otherFace(WHITE).color;
						cube.rotate(sideColor, cube.CW);
						otherSideColor = pTarget.otherFace(sideColor).color;
						while(cube.at(WHITE, otherSideColor).getTile(WHITE) != null) cube.rotate(WHITE, cube.CW); 
						cube.rotate(otherSideColor, cube.CW);
					}
					break;
				case 2:
					whiteSideColor = pTarget.getTile(WHITE).face.color;
					otherSideColor = pTarget.otherFace(whiteSideColor).color;
					while(cube.at(WHITE, otherSideColor).getTile(WHITE) != null) cube.rotate(WHITE, cube.CW);
					int dirToRotate = cube.faces.get(WHITE).relativeDir(otherSideColor, whiteSideColor);
					cube.rotate(otherSideColor, dirToRotate);
					break;
				case 3:
					if(pTarget.getTile(WHITE).facing(YELLOW)) {
						otherSideColor = pTarget.otherFace(YELLOW).color;
						while(cube.at(WHITE, otherSideColor).getTile(WHITE) != null) cube.rotate(WHITE, cube.CW);
						cube.rotate(otherSideColor, cube.DBL_CW);
					} else {
						sideColor = pTarget.getTile(WHITE).face.color;
						while(cube.at(WHITE, sideColor).getTile(WHITE) != null) cube.rotate(WHITE, cube.CW);
						cube.rotate(sideColor, cube.CW);
						cube.rotate(WHITE, cube.CW);	
						otherSideColor = pTarget.otherFace(sideColor).color;
						cube.rotate(otherSideColor, cube.CCW);
					}
					break;
			}
		}
	}

	public void fixWhiteCross() throws SolverException {
		if(!checkCross(WHITE, false)) throw new SolverException("Attempting to fix white cross before white cross is gotten");

		int tries = 0;
		while(tries < 4 && numFixed(WHITE, Cube.Type.EDGE) < 2) {
			cube.rotate(WHITE, Cube.CW);
			tries++;
		}
		if(tries == 4) throw new SolverException("Could not align white cross");
		
		if(numFixed(WHITE, Cube.Type.EDGE) == 4) return;

		Cube.Color c1 = null, c2 = null;
		Cube.Piece p;
		for(Cube.Spot spot : edges(WHITE)) {
			p = cube.pieces.get(spot);
			if(!isFixed(p)) {
				if(c1 == null) 
					c1 = p.otherFace(WHITE).color;
				else 
					c2 = p.otherFace(WHITE).color;
			}
		}

		cube.rotate(c1, Cube.CW);
		int c2Toc1 = cube.faces.get(WHITE).relativeDir(c2, c1);
		cube.rotate(WHITE, c2Toc1);
		cube.rotate(c1, Cube.CCW);
		int c1Toc2 = -1 * c2Toc1;
		cube.rotate(WHITE, c1Toc2);
		cube.rotate(c1, Cube.CW);
	}

	public void getWhiteCorners() throws SolverException {
		if(!checkCross(WHITE, true)) throw new SolverException("Attempting to start white corners when white cross isn't solved");	

		Cube.Color whiteSideColor, otherSideColor, sideColor;
		for(Cube.Spot corner : corners(WHITE)) {
			Cube.Piece pTarget = cube.pieces.get(corner);
			switch(pTarget.layer()) {
				case 1:
					if(cube.at(pTarget.colors()) == pTarget) {
						if(pTarget.getTile(WHITE).facing(WHITE)) {
							break;
						} else {
							whiteSideColor = pTarget.getTile(WHITE).face.color;
							otherSideColor = pTarget.otherFace(WHITE, whiteSideColor).color;
							int up = cube.faces.get(whiteSideColor).relativeDir(otherSideColor, YELLOW);
							cube.rotate(whiteSideColor, up);
							int away = cube.faces.get(YELLOW).relativeDir(whiteSideColor, otherSideColor);
							cube.rotate(YELLOW, away);
							int down = -1 * up;
							cube.rotate(whiteSideColor, down);
							int towards = -1 * away;
							cube.rotate(YELLOW, towards);
						}
					} else {
						sideColor = pTarget.getTile(WHITE).face.color;
						// If white facing down, pick a random other side
						if(sideColor == WHITE) sideColor = pTarget.getTile(pTarget.otherColors(WHITE)[0]).face.color;
						otherSideColor = pTarget.otherFace(WHITE, sideColor).color;
						int up = cube.faces.get(sideColor).relativeDir(otherSideColor, YELLOW);
						cube.rotate(sideColor, up);
						int away = cube.faces.get(YELLOW).relativeDir(sideColor, otherSideColor);
						cube.rotate(YELLOW, away);
						int down = -1 * up;
						cube.rotate(sideColor, down);
					}
				case 3:
					// Get into corresponding third layer spot
					Cube.Color[] nonWhite = pTarget.otherColors(WHITE);
					int leftSideIdx = cube.faces.get(WHITE).relativeDir(nonWhite[0], nonWhite[1]) == Cube.CW ? 0 : 1;
					Cube.Color leftSide = nonWhite[leftSideIdx], rightSide = nonWhite[1 - leftSideIdx];
					Cube.Spot targetSpot = cube.spot(YELLOW, leftSide, rightSide);
					while(!pTarget.spot().equals(targetSpot)) {
						cube.rotate(YELLOW, Cube.CW);
					}
					if(pTarget.getTile(WHITE).facing(YELLOW)) {
						// Get white tile facing side
						cube.execute(new String[]{ "F", "UU", "F'", "U'" }, rightSide);
					}

					if(pTarget.getTile(WHITE).facing(rightSide)) {
						cube.execute(new String[]{ "F", "U", "F'" }, rightSide);
					} else {
						cube.execute(new String[]{ "F'", "U'", "F"}, leftSide);
					}
					break;
			}
		}
	}

	public boolean checkFirstLayer() {
		return checkCross(WHITE, true) && (numFixed(WHITE, Cube.Type.CORNER) == 4);
	}

	/** SECOND LAYER **/

	public Cube.Spot[] l2Edges() {
		Cube.Color[] sides = cube.faces.get(WHITE).adjs;
		Cube.Spot[] edges = new Cube.Spot[4];
		for(int i = 0; i < 4; i++) {
			edges[i] = cube.spot(sides[i], sides[(i+1)%4]);
		}
		return edges;
	}

	public void getSecondLayer() throws SolverException {
		if(!checkFirstLayer()) throw new SolverException("Attempting to get second layer before getting first layer");

		String[] frAlgo = new String[]{ "R", "U'", "R'", "U'", "F'", "U", "F" };
		String[] flAlgo = new String[]{ "L'", "U", "L", "U", "F", "U'", "F'" };

		for(Cube.Spot edge : l2Edges()) {
			Cube.Piece pTarget = cube.pieces.get(edge);
			switch(pTarget.layer()) {
				case 2:
					if(isFixed(pTarget)) {
						break;
					} else {
						Cube.Color[] sides = new Cube.Color[2];
						sides[0] = pTarget.getTiles()[0].face.color;
						sides[1] = pTarget.getTiles()[1].face.color;
						Cube.Color front = cube.faces.get(WHITE).relativeDir(sides[0], sides[1]) == Cube.CW ? sides[0] : sides[1];
						cube.execute(frAlgo, front);
					}
				case 3:
					Cube.Color topColor = pTarget.getTile(cube.faces.get(YELLOW)).color;
					Cube.Spot targetSpot = cube.spot(YELLOW, cube.faces.get(YELLOW).nextColor(topColor, Cube.DBL_CW));
					while(!pTarget.spot().equals(targetSpot)) cube.rotate(YELLOW, Cube.CW);
					Cube.Color front = pTarget.otherColor(topColor);
					if(cube.faces.get(WHITE).relativeDir(front, topColor) == Cube.CW) {
						cube.execute(frAlgo, front);
					} else {
						cube.execute(flAlgo, front);
					}
					break;
			}
		}
	}

	public boolean checkSecondLayer() {
		for(Cube.Spot spot : l2Edges()) {
			if(!isFixed(cube.pieces.get(spot))) return false;
		}
		return true;
	}

	/** THIRD LAYER **/

	public void getYellowCross() throws SolverException {
		if(!(checkFirstLayer() && checkSecondLayer())) throw new SolverException("Attempting to get yellow cross before first and second layers gotten");
		Cube.Color[] sides = cube.faces.get(YELLOW).adjs;
		Cube.Color f = sides[0], l = sides[1], b = sides[2], r = sides[3];
		Cube.Color front;
		while(!checkCross(YELLOW, false)) {
			if(cube.at(YELLOW, f).getTile(YELLOW).facing(YELLOW)) {
				front = (cube.at(YELLOW, l).getTile(YELLOW).facing(YELLOW) || cube.at(YELLOW, b).getTile(YELLOW).facing(YELLOW)) ?
							r : b;
			} else {
				front = (cube.at(YELLOW, l).getTile(YELLOW).facing(YELLOW)) ?
							f : l;
			}
			cube.execute(new String[]{ "F", "R", "U", "R'", "U'", "F'" }, front);
		}
	}

	public void fixYellowCross() throws SolverException {
		if(!checkCross(YELLOW, false)) throw new SolverException("Attempting to fix yellow cross before getting yellow cross");
		while(!checkCross(YELLOW, true)) {
			while(numFixed(YELLOW, Cube.Type.EDGE) < 2) cube.rotate(YELLOW, Cube.CW);
			if(checkCross(YELLOW, true)) break;

			Cube.Color[] sides = cube.faces.get(YELLOW).adjs;
			for(int i = 0; i < 4; i++) {
				if(!isFixed(cube.at(YELLOW, sides[(i+1) % 4])) && isFixed(cube.at(YELLOW, sides[(i+2) % 4]))) {
					cube.execute(new String[] { "R", "U", "R'", "U", "R", "UU", "R'" }, sides[i]);
					break;
				}
			}
		}
	}

	public void positionYellowCorners() throws SolverException {
		if(!checkCross(YELLOW, true)) throw new SolverException("Attempting to position yellow corners before yellow cross is fixed");
		while(numPositioned(YELLOW, Cube.Type.CORNER) < 4) {
			Cube.Color[] sides = cube.faces.get(YELLOW).adjs;
			Cube.Color front = sides[0];
			for(int i = 1; i < 4; i++) {
				Cube.Spot corner = cube.spot(YELLOW, sides[i], sides[(i+3) % 4]);
				if(cube.at(corner).colors().equals(corner)) {
					front = sides[i];
					break;
				}
			}
			cube.execute(new String[]{ "U", "R", "U'", "L'", "U", "R'", "U'", "L" }, front);
		}
	}

	public void fixYellowCorners() throws SolverException {
		if(numPositioned(YELLOW, Cube.Type.CORNER) != 4) throw new SolverException("Attempting to fix yellow corners before yellow corners are positioned");
		if(numFixed(YELLOW, Cube.Type.CORNER) == 4) return;
		Cube.Color[] sides = cube.faces.get(YELLOW).adjs;
		Cube.Color f = sides[1], r = sides[0];
		for(int i = 0; i < 4; i++) {
			r = sides[i]; f = sides[(i+1) % 4];
			if(!isFixed(cube.at(YELLOW, f, r))) break;
		}
		for(int i = 0; i < 4; i++) {
			while(!cube.at(YELLOW, f, r).getTile(YELLOW).facing(YELLOW)) {
				for(int j = 0; j < 2; j++) cube.execute(new String[]{ "R'", "D'", "R", "D" }, f);
			}
			cube.rotate(YELLOW, Cube.CW);
		}
	}

	public boolean checkThirdLayer() {
		return (checkCross(YELLOW, true) && numFixed(YELLOW, Cube.Type.CORNER) == 4);
	}

}
