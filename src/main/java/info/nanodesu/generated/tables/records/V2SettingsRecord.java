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
public class V2SettingsRecord extends org.jooq.impl.TableRecordImpl<info.nanodesu.generated.tables.records.V2SettingsRecord> implements org.jooq.Record1<java.lang.Integer> {

	private static final long serialVersionUID = -1508091205;

	/**
	 * Setter for <code>public.v2_settings.report_version</code>. 
	 */
	public void setReportVersion(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.v2_settings.report_version</code>. 
	 */
	public java.lang.Integer getReportVersion() {
		return (java.lang.Integer) getValue(0);
	}

	// -------------------------------------------------------------------------
	// Record1 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row1<java.lang.Integer> fieldsRow() {
		return (org.jooq.Row1) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row1<java.lang.Integer> valuesRow() {
		return (org.jooq.Row1) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return info.nanodesu.generated.tables.V2Settings.V2_SETTINGS.REPORT_VERSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getReportVersion();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached V2SettingsRecord
	 */
	public V2SettingsRecord() {
		super(info.nanodesu.generated.tables.V2Settings.V2_SETTINGS);
	}

	/**
	 * Create a detached, initialised V2SettingsRecord
	 */
	public V2SettingsRecord(java.lang.Integer reportVersion) {
		super(info.nanodesu.generated.tables.V2Settings.V2_SETTINGS);

		setValue(0, reportVersion);
	}
}
