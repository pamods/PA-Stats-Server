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
public class V2PlanetJsonRecord extends org.jooq.impl.UpdatableRecordImpl<info.nanodesu.generated.tables.records.V2PlanetJsonRecord> implements org.jooq.Record2<java.lang.Integer, java.lang.String> {

	private static final long serialVersionUID = -891348012;

	/**
	 * Setter for <code>public.v2_planet_json.id</code>. 
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.v2_planet_json.id</code>. 
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>public.v2_planet_json.planet</code>. 
	 */
	public void setPlanet(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.v2_planet_json.planet</code>. 
	 */
	public java.lang.String getPlanet() {
		return (java.lang.String) getValue(1);
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
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.String> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.String> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return info.nanodesu.generated.tables.V2PlanetJson.V2_PLANET_JSON.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return info.nanodesu.generated.tables.V2PlanetJson.V2_PLANET_JSON.PLANET;
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
	public java.lang.String value2() {
		return getPlanet();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached V2PlanetJsonRecord
	 */
	public V2PlanetJsonRecord() {
		super(info.nanodesu.generated.tables.V2PlanetJson.V2_PLANET_JSON);
	}

	/**
	 * Create a detached, initialised V2PlanetJsonRecord
	 */
	public V2PlanetJsonRecord(java.lang.Integer id, java.lang.String planet) {
		super(info.nanodesu.generated.tables.V2PlanetJson.V2_PLANET_JSON);

		setValue(0, id);
		setValue(1, planet);
	}
}
