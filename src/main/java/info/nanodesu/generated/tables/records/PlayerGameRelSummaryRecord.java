/**
 * This class is generated by jOOQ
 */
package info.nanodesu.generated.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerGameRelSummaryRecord extends org.jooq.impl.TableRecordImpl<info.nanodesu.generated.tables.records.PlayerGameRelSummaryRecord> implements org.jooq.Record2<java.lang.Integer, java.lang.Boolean> {

	private static final long serialVersionUID = -617982168;

	/**
	 * Setter for <code>public.player_game_rel_summary.id</code>. 
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.player_game_rel_summary.id</code>. 
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>public.player_game_rel_summary.reported</code>. 
	 */
	public void setReported(java.lang.Boolean value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.player_game_rel_summary.reported</code>. 
	 */
	public java.lang.Boolean getReported() {
		return (java.lang.Boolean) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Boolean> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Boolean> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return info.nanodesu.generated.tables.PlayerGameRelSummary.PLAYER_GAME_REL_SUMMARY.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field2() {
		return info.nanodesu.generated.tables.PlayerGameRelSummary.PLAYER_GAME_REL_SUMMARY.REPORTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value2() {
		return getReported();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached PlayerGameRelSummaryRecord
	 */
	public PlayerGameRelSummaryRecord() {
		super(info.nanodesu.generated.tables.PlayerGameRelSummary.PLAYER_GAME_REL_SUMMARY);
	}

	/**
	 * Create a detached, initialised PlayerGameRelSummaryRecord
	 */
	public PlayerGameRelSummaryRecord(java.lang.Integer id, java.lang.Boolean reported) {
		super(info.nanodesu.generated.tables.PlayerGameRelSummary.PLAYER_GAME_REL_SUMMARY);

		setValue(0, id);
		setValue(1, reported);
	}
}
