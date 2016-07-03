import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Cube {

	public static enum Color {
		WHITE(0), RED(1), GREEN(2), BLUE(3), ORANGE(4), YELLOW(5);
		public int val;
		Color(int val) { this.val = val; }
		public String toString() {
			switch(val) {
				case 0: return "W";
				case 1: return "R";
				case 2: return "G";
				case 3: return "B";
				case 4: return "O";
				case 5: return "Y";
				default: return "N";
			}
		}
	}

	public static final Color WHITE = Color.WHITE;
	public static final Color YELLOW = Color.YELLOW;
	public static final Color RED = Color.RED;
	public static final Color GREEN = Color.GREEN;
	public static final Color BLUE = Color.BLUE;
	public static final Color ORANGE = Color.ORANGE;

	public static enum Type {	   
		EDGE, CORNER
	}

	public static final int DBL_CW = 2;
	public static final int CW = 1;
	public static final int CCW = -1;
	public static final int DBL_CCW = -2;

	public static class Tile {
		public final Color color;
		public Face face;
		
		public Tile(Color color) {
			this.color = color;
		}

		public Tile(Color color, Face face) {
			this.color = color;
			this.face = face;
		}

		public boolean facing(Face f) {
			return this.face == f;
		}

		public boolean facing(Color c) {
			return this.face.color == c;
		}

		public String toString() {
			return "T(" + color.toString() + ")";
		}
	}

	public static class Spot {

		public List<Color> colors;

		public Spot(Color... colors) {
			this.colors = new ArrayList<Color>();
			for(Color c : colors) {
				this.colors.add(c);
			}
		}

		@Override
		public int hashCode() {
			int hc = 0;
			for(Color c : colors) {
				if(c != null) hc |= (1 << c.val);
			}
			return hc;
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof Spot)) return false;
			return (((Spot) other) != null) && (((Spot) other).hashCode() == this.hashCode());
		}

		public String toString() {
			return colors.toString();
		}
	
	}

	public class Piece {
		private Type type;
		private Tile[] tiles;

		public Piece(Type type) {
			this.type = type;
			tiles = new Tile[type == Type.EDGE ? 2 : 3];
		}

		public Piece(Spot spot, boolean initialize) {
			this.type = spot.colors.size() == 2 ? Type.EDGE : Type.CORNER;
			tiles = new Tile[type == Type.EDGE ? 2 : 3];
			if(initialize) {
				for(int i = 0; i < tiles.length; i++) {
					Color c = spot.colors.get(i);
					tiles[i] = new Tile(c, faces.get(c));
				}
				pieces.put(spot, this);
			}
		}

		public void addTile(Color color, Face face) {
			for(int i = 0; i < tiles.length; i++) {
				if(tiles[i] == null) {
					tiles[i] = new Tile(color, face);
					return;
				}
			}
		}

		public Tile[] getTiles() { return tiles; }

		public Spot spot() {
			if(type == Type.EDGE) return new Spot(tiles[0].face.color, tiles[1].face.color);
			else return new Spot(tiles[0].face.color, tiles[1].face.color, tiles[2].face.color);
		}

		public Spot colors() {
			if(type == Type.EDGE) return new Spot(tiles[0].color, tiles[1].color);
			else return new Spot(tiles[0].color, tiles[1].color, tiles[2].color);
		}

		public boolean facing(Face f) {
			for(Tile t : tiles) {
				if(t.facing(f)) return true;
			}
			return false;
		}

		public Face otherFace(Color... colors) {
			if(this.type == Type.EDGE) {
				Color not = colors[0];
				for(Tile t : tiles) {
					if(!t.facing(not)) return t.face;
				}
			} else {
				Color not1 = colors[0];
				Color not2 = colors[1];
				for(Tile t : tiles) {
					if(!t.facing(not1) && !t.facing(not2)) return t.face;
				}
			}
			return null;
		}

		public Color otherColor(Color not) {
			for(Tile t : tiles) {
				if(t.color != not) return t.color;
			}
			return null;
		}

		public Color[] otherColors(Color not) {
			Color[] others = new Color[2];
			for(Tile t : tiles) {
				if(t.color != not && others[0] == null) others[0] = t.color;
				else if(t.color != not) others[1] = t.color;
			}
			return others;
		}

		public Tile getTile(Color c) {
			for(Tile t : tiles) {
				if(t.color == c) return t;
			}
			return null;
		}

		public Tile getTile(Face f) {
			for(Tile t : tiles) {
				if(t.facing(f)) return t;
			}
			return null;
		}

		public int layer() {
			if(facing(faces.get(WHITE))) return 1;
			if(facing(faces.get(YELLOW))) return 3;
			return 2;
		}

		public String toString() {
			String out =  "P(" + tiles[0].toString() + "," + tiles[1].toString();
			if(type == Type.CORNER) out += "," + tiles[2].toString(); 
			return out + ") at " + spot().toString();
		}

		@Override
		public int hashCode() {
			int hc = 0;
			for(Tile t : tiles) {
				if(t != null && t.color != null) hc |= (1 << t.color.val);
			}
			return hc;
		}

		@Override
		public boolean equals(Object other) {
			return (((Piece) other) != null) && (((Piece) other).hashCode() == this.hashCode());
		}

	}
	
	public class Face {
		
		public Color color;
		public Color[] adjs;

		public Face(Color color, Color[] adjs) {
			this.color = color;
			this.adjs = adjs;
		}

		public Piece[] pieces() {
			Piece[] pcs = new Piece[8];
			for(int i = 0; i < 4; i++) {
				Color curC = adjs[i];
				Color nextC = adjs[(i+1) % 4];
				pcs[2*i] = pieceAt.get(spot(this.color, curC));
				pcs[(2*i)+1] = pieceAt.get(spot(this.color, curC, nextC));
			}
			return pcs;
		}

		public void rotate(int dir) {
			Piece[] pcs = pieces();
			for(Piece p : pcs) {
				for(Tile t : p.getTiles()) {
					if(!t.facing(this)) t.face = faces.get(nextColor(t.face.color, dir));
				}
				pieceAt.put(p.spot(), p);
			}
		}

		public Color nextColor(Color c, int dir) {
			int idx;	
			for(idx = 0; idx < 4; idx++) {
				if(adjs[idx] == c) break;
			}
			return adjs[(idx + dir + 4) % 4];
		}

		public int relativeDir(Color c1, Color c2) {
			int idx1 = 0, idx2 = 0;
			for(int i = 0; i < 4; i++) {
				if(adjs[i] == c1) idx1 = i;
				if(adjs[i] == c2) idx2 = i;
			}
			int diff = Math.abs(idx1 - idx2);
			if(diff == 2) { return DBL_CW; }
			else if((diff == 1 && idx1 < idx2) || (diff == 3 && idx1 > idx2)) {
				return CW;
			} else {
				return CCW;
			}
		}

		public String toString() {
			Piece[] pcs = pieces();
			return "" + 
				"|" + pcs[7].getTile(this).color + "|" + pcs[0].getTile(this).color + "|" + pcs[1].getTile(this).color + "|\n" +
				"|" + pcs[6].getTile(this).color + "|" + this.color + "|" + pcs[2].getTile(this).color + "|\n" + 
				"|" + pcs[5].getTile(this).color + "|" + pcs[4].getTile(this).color + "|" + pcs[3].getTile(this).color + "|\n"; 
		}
	}

	public HashMap<Color, Face> faces;
	public HashMap<Spot, Piece> pieces;
	public HashMap<Spot, Piece> pieceAt;
	public ArrayList<String> moveLog;

	public Spot spot(Color... colors) {
		return new Spot(colors);
	}

	public Cube(boolean initialize) {
		pieces = new HashMap<Spot, Piece>();
		pieceAt = new HashMap<Spot, Piece>();
		faces = new HashMap<Color, Face>();
		moveLog = new ArrayList<String>();

		faces.put(WHITE, new Face(WHITE, new Color[]{ RED, GREEN, ORANGE, BLUE }));
		faces.put(RED, new Face(RED, new Color[]{ WHITE, BLUE, YELLOW, GREEN }));
		faces.put(GREEN, new Face(GREEN, new Color[]{ WHITE, RED, YELLOW, ORANGE }));
		faces.put(BLUE, new Face(BLUE, new Color[]{ WHITE, ORANGE, YELLOW, RED }));
		faces.put(ORANGE, new Face(ORANGE, new Color[]{ WHITE, GREEN, YELLOW, BLUE }));
		faces.put(YELLOW, new Face(YELLOW, new Color[]{ RED, BLUE, ORANGE, GREEN }));

		Face wFace = faces.get(WHITE);
		Face yFace = faces.get(YELLOW);
		Color[] sides = wFace.adjs;

		for(int i = 0; i < 4; i++) {
			Color curC = sides[i];
			Color nextC = sides[(i+1) % 4];

			Spot wEdge = spot(WHITE, curC);
			Piece wePiece = new Piece(wEdge, initialize);
			pieceAt.put(wEdge, wePiece); 

			Spot yEdge = spot(YELLOW, curC);
			Piece yePiece = new Piece(yEdge, initialize);
			pieceAt.put(yEdge, yePiece);

			Spot sEdge = spot(curC, nextC);
			Piece sePiece = new Piece(sEdge, initialize);
			pieceAt.put(sEdge, sePiece);

			Spot wCorner = spot(WHITE, curC, nextC);
			Piece wcPiece = new Piece(wCorner, initialize);
			pieceAt.put(wCorner, wcPiece);

			Spot yCorner = spot(YELLOW, curC, nextC);
			Piece ycPiece = new Piece(yCorner, initialize);
			pieceAt.put(yCorner, ycPiece);
		}
	}

	public Piece at(Color... colors) {
		return pieceAt.get(spot(colors));
	}

	public Piece at(Spot spot) {
		return pieceAt.get(spot);
	}

	public void rotate(Color c, int dir) {
		faces.get(c).rotate(dir);
		String toLog = "";
		switch(dir) {
			case CW: toLog = c.toString(); break;
			case CCW: toLog = c.toString() + "'"; break;
			case DBL_CW:
			case DBL_CCW: toLog = c.toString() + c.toString(); break;
		}
		log(toLog);
	}

	public void mix(int num) {
		Color[] colors = { WHITE, GREEN, RED, BLUE, ORANGE, YELLOW };
		int[] moves = { DBL_CCW, CCW, CW, DBL_CW };
		Random rand = new Random();
		for(int i = 0; i < num; i++) {
			Color c = colors[rand.nextInt(6)];
			int move = moves[rand.nextInt(4)];
			rotate(c, move);
		}
	}

	public boolean isMove(String s) {
		if(s == null || s.equals("")) return false;
		char first = s.charAt(0);
		if(!Character.isLetter(first)) return false;
		if(s.length() == 1) return true;
		if(s.length() == 2 && s.charAt(1) == first) return true;
		if(s.length() == 2 && s.charAt(1) == '\'') return true;
		return false;
	}

	public void execute(String[] moves, Color front) {
		HashMap<String, Color> relativeFace = new HashMap<String, Color>();
		relativeFace.put("U", YELLOW);
		relativeFace.put("D", WHITE);
		Color[] sides = faces.get(WHITE).adjs;
		for(int i = 0; i < 4; i++) {
			if(sides[i] == front) {
				relativeFace.put("F", sides[i]);
				relativeFace.put("R", sides[(i+1)%4]);
				relativeFace.put("B", sides[(i+2)%4]);
				relativeFace.put("L", sides[(i+3)%4]);
				break;
			}
		}
		
		for(int i = 0; i < moves.length; i++) {
			if(isMove(moves[i])) {
				String let = moves[i].substring(0, 1);
				int dir;
				if(moves[i].length() == 1) {
					dir = CW;
				} else if(moves[i].charAt(1) == '\'') {
					dir = CCW;
				} else {
					dir = DBL_CW;
				}
				rotate(relativeFace.get(let), dir);
			}
		}
	}

	public void log(String s) {
		moveLog.add(s);
	}

	public void compressLog() {
		int i = 0;
		String cur, next, next2;
		while(i < log.size() - 2) {
			cur = log.get(i);
			next = log.get(i+1);
			next2 = log.get(i+2);
			if(isMove(cur) && isMove(next)) {
				if(cur.equals(next + "'") || next.equals(cur + "'")) {
					log.remove(i); log.remove(i+1); i--;
				} else if(((cur.length() == 1) || (cur.length() == 2 && cur.charAt(1) == '\'')) && cur.equals(next)) {
					log.set(i, cur.substring(0, 1) + cur.substring(0, 1));
					log.remove(i+1); i--;
				}
			}
		}
	}

	public ArrayList<String> getLog() {
		return moveLog;
	}

	public String toString() {
		String out = "";
		Color[] colors = { WHITE, GREEN, RED, BLUE, ORANGE, YELLOW };
		for(Color c : colors) {
			out += faces.get(c).toString() + "\n";
		}
		return out;
	}

}
