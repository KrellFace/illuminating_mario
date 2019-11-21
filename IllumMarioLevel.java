package illumsearch;

import engine.core.MarioLevel;

public class IllumMarioLevel extends MarioLevel {
	
	private String levelRep;
	
	public IllumMarioLevel(String level, boolean visuals) {

		super(level, visuals);
		levelRep = level;
		
	}
	
	public IllumMarioLevel clone() {
		IllumMarioLevel level = (IllumMarioLevel) super.clone();
		level.setStringRep(levelRep);
		return level;

	}
	
	public void setStringRep(String level) {
		levelRep = level;
	}
	
	public String getStringRep() {
		return levelRep;
	}

}
