package game.board.oop.entities;

import game.board.oop.EEntity;
import game.board.oop.Tile;

public class EntityFactory {

	public static Entity createEntity(EEntity type, Tile tile) {
		if (type == null) return null;
		if (type == EEntity.NONE) return null;
		if (type.isPlayer()) return new PlayerEntity(type, tile);
		if (type.isSomeBox()) return new BoxEntity(type, tile);
		return new Entity(type, tile);
		
	}
	
}
