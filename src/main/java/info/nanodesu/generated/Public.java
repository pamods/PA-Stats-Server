/**
 * This class is generated by jOOQ
 */
package info.nanodesu.generated;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -199631737;

	/**
	 * The singleton instance of <code>public</code>
	 */
	public static final Public PUBLIC = new Public();

	/**
	 * No further instances allowed
	 */
	private Public() {
		super("public");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			info.nanodesu.generated.Sequences.TEAM_ID,
			info.nanodesu.generated.Sequences.V2_DISPLAY_NAME_HISTORY_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_GAME_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_PLANET_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_PLAYER_DISPLAY_NAME_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_PLAYER_GAME_REL_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_PLAYER_ID_SEQ,
			info.nanodesu.generated.Sequences.V2_TIMEPOINT_STATS_ID_SEQ);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			info.nanodesu.generated.tables.PlayerGameRelSummary.PLAYER_GAME_REL_SUMMARY,
			info.nanodesu.generated.tables.V2DisplayNameHistory.V2_DISPLAY_NAME_HISTORY,
			info.nanodesu.generated.tables.V2Game.V2_GAME,
			info.nanodesu.generated.tables.V2Planet.V2_PLANET,
			info.nanodesu.generated.tables.V2Player.V2_PLAYER,
			info.nanodesu.generated.tables.V2PlayerDisplayName.V2_PLAYER_DISPLAY_NAME,
			info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL,
			info.nanodesu.generated.tables.V2Settings.V2_SETTINGS,
			info.nanodesu.generated.tables.V2Teams.V2_TEAMS,
			info.nanodesu.generated.tables.V2TimepointStats.V2_TIMEPOINT_STATS);
	}
}
