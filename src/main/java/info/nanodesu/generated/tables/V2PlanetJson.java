/**
 * This class is generated by jOOQ
 */
package info.nanodesu.generated.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class V2PlanetJson extends org.jooq.impl.TableImpl<info.nanodesu.generated.tables.records.V2PlanetJsonRecord> {

	private static final long serialVersionUID = -480963738;

	/**
	 * The singleton instance of <code>public.v2_planet_json</code>
	 */
	public static final info.nanodesu.generated.tables.V2PlanetJson V2_PLANET_JSON = new info.nanodesu.generated.tables.V2PlanetJson();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<info.nanodesu.generated.tables.records.V2PlanetJsonRecord> getRecordType() {
		return info.nanodesu.generated.tables.records.V2PlanetJsonRecord.class;
	}

	/**
	 * The column <code>public.v2_planet_json.id</code>. 
	 */
	public final org.jooq.TableField<info.nanodesu.generated.tables.records.V2PlanetJsonRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this);

	/**
	 * The column <code>public.v2_planet_json.planet</code>. 
	 */
	public final org.jooq.TableField<info.nanodesu.generated.tables.records.V2PlanetJsonRecord, java.lang.String> PLANET = createField("planet", org.jooq.impl.SQLDataType.CLOB.nullable(false), this);

	/**
	 * Create a <code>public.v2_planet_json</code> table reference
	 */
	public V2PlanetJson() {
		super("v2_planet_json", info.nanodesu.generated.Public.PUBLIC);
	}

	/**
	 * Create an aliased <code>public.v2_planet_json</code> table reference
	 */
	public V2PlanetJson(java.lang.String alias) {
		super(alias, info.nanodesu.generated.Public.PUBLIC, info.nanodesu.generated.tables.V2PlanetJson.V2_PLANET_JSON);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<info.nanodesu.generated.tables.records.V2PlanetJsonRecord, java.lang.Integer> getIdentity() {
		return info.nanodesu.generated.Keys.IDENTITY_V2_PLANET_JSON;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<info.nanodesu.generated.tables.records.V2PlanetJsonRecord> getPrimaryKey() {
		return info.nanodesu.generated.Keys.V2_PLANET_JSON_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<info.nanodesu.generated.tables.records.V2PlanetJsonRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<info.nanodesu.generated.tables.records.V2PlanetJsonRecord>>asList(info.nanodesu.generated.Keys.V2_PLANET_JSON_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public info.nanodesu.generated.tables.V2PlanetJson as(java.lang.String alias) {
		return new info.nanodesu.generated.tables.V2PlanetJson(alias);
	}
}