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
public class V2TeamsRecord extends org.jooq.impl.UpdatableRecordImpl<info.nanodesu.generated.tables.records.V2TeamsRecord> implements org.jooq.Record4<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = -1351992411;

	/**
	 * Setter for <code>public.v2_teams.id</code>. 
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.v2_teams.id</code>. 
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>public.v2_teams.ingame_id</code>. 
	 */
	public void setIngameId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.v2_teams.ingame_id</code>. 
	 */
	public java.lang.Integer getIngameId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>public.v2_teams.primary_color</code>. 
	 */
	public void setPrimaryColor(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>public.v2_teams.primary_color</code>. 
	 */
	public java.lang.String getPrimaryColor() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>public.v2_teams.secondary_color</code>. 
	 */
	public void setSecondaryColor(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>public.v2_teams.secondary_color</code>. 
	 */
	public java.lang.String getSecondaryColor() {
		return (java.lang.String) getValue(3);
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
	// Record4 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row4<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row4) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row4<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row4) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return info.nanodesu.generated.tables.V2Teams.V2_TEAMS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return info.nanodesu.generated.tables.V2Teams.V2_TEAMS.INGAME_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return info.nanodesu.generated.tables.V2Teams.V2_TEAMS.PRIMARY_COLOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return info.nanodesu.generated.tables.V2Teams.V2_TEAMS.SECONDARY_COLOR;
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
		return getIngameId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getPrimaryColor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getSecondaryColor();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached V2TeamsRecord
	 */
	public V2TeamsRecord() {
		super(info.nanodesu.generated.tables.V2Teams.V2_TEAMS);
	}

	/**
	 * Create a detached, initialised V2TeamsRecord
	 */
	public V2TeamsRecord(java.lang.Integer id, java.lang.Integer ingameId, java.lang.String primaryColor, java.lang.String secondaryColor) {
		super(info.nanodesu.generated.tables.V2Teams.V2_TEAMS);

		setValue(0, id);
		setValue(1, ingameId);
		setValue(2, primaryColor);
		setValue(3, secondaryColor);
	}
}
