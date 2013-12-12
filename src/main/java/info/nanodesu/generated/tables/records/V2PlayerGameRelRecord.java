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
public class V2PlayerGameRelRecord extends org.jooq.impl.UpdatableRecordImpl<info.nanodesu.generated.tables.records.V2PlayerGameRelRecord> implements org.jooq.Record6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Boolean, java.lang.Boolean, java.lang.Integer> {

	private static final long serialVersionUID = -2026769531;

	/**
	 * Setter for <code>public.v2_player_game_rel.id</code>. 
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.id</code>. 
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>public.v2_player_game_rel.p</code>. 
	 */
	public void setP(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.p</code>. 
	 */
	public java.lang.Integer getP() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>public.v2_player_game_rel.g</code>. 
	 */
	public void setG(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.g</code>. 
	 */
	public java.lang.Integer getG() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>public.v2_player_game_rel.died</code>. 
	 */
	public void setDied(java.lang.Boolean value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.died</code>. 
	 */
	public java.lang.Boolean getDied() {
		return (java.lang.Boolean) getValue(3);
	}

	/**
	 * Setter for <code>public.v2_player_game_rel.locked</code>. 
	 */
	public void setLocked(java.lang.Boolean value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.locked</code>. 
	 */
	public java.lang.Boolean getLocked() {
		return (java.lang.Boolean) getValue(4);
	}

	/**
	 * Setter for <code>public.v2_player_game_rel.t</code>. 
	 */
	public void setT(java.lang.Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>public.v2_player_game_rel.t</code>. 
	 */
	public java.lang.Integer getT() {
		return (java.lang.Integer) getValue(5);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record6 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Boolean, java.lang.Boolean, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row6) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Boolean, java.lang.Boolean, java.lang.Integer> valuesRow() {
		return (org.jooq.Row6) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.P;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.G;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field4() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.DIED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field5() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.LOCKED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL.T;
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
	public java.lang.Integer value2() {
		return getP();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getG();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value4() {
		return getDied();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value5() {
		return getLocked();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getT();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached V2PlayerGameRelRecord
	 */
	public V2PlayerGameRelRecord() {
		super(info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL);
	}

	/**
	 * Create a detached, initialised V2PlayerGameRelRecord
	 */
	public V2PlayerGameRelRecord(java.lang.Integer id, java.lang.Integer p, java.lang.Integer g, java.lang.Boolean died, java.lang.Boolean locked, java.lang.Integer t) {
		super(info.nanodesu.generated.tables.V2PlayerGameRel.V2_PLAYER_GAME_REL);

		setValue(0, id);
		setValue(1, p);
		setValue(2, g);
		setValue(3, died);
		setValue(4, locked);
		setValue(5, t);
	}
}