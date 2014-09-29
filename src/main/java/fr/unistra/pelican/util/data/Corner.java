package fr.unistra.pelican.util.data;
/**
 * @author Xavier Philippeau (source : http://www.developpez.net/forums/d325133/general-developpement/algorithme-mathematiques/contribuez/image-detecteur-harris-imagej/#post3363731)
 * @author Julien Bidolet (adaptation for pelican)
 */

	public class Corner {
		private int x,y; // corner position
		private float h; // harris measure
		
		public Corner(int x, int y, float h) {
			this.x=x; this.y=y; this.h=h;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public float getH() {
			return h;
		}

		public void setH(float h) {
			this.h = h;
		}
	}
