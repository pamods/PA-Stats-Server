/**
 * This class is generated by jOOQ
 */
package info.nanodesu.generated.routines;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ConvertToJson extends org.jooq.impl.AbstractRoutine<java.lang.Object> {

	private static final long serialVersionUID = 626697059;

	/**
	 * The parameter <code>public.convert_to_json.RETURN_VALUE</code>. 
	 */
	public static final org.jooq.Parameter<java.lang.Object> RETURN_VALUE = createParameter("RETURN_VALUE", org.jooq.impl.DefaultDataType.getDefaultDataType("json"));

	/**
	 * The parameter <code>public.convert_to_json.v_input</code>. 
	 */
	public static final org.jooq.Parameter<java.lang.String> V_INPUT = createParameter("v_input", org.jooq.impl.SQLDataType.CLOB);

	/**
	 * Create a new routine call instance
	 */
	public ConvertToJson() {
		super("convert_to_json", info.nanodesu.generated.Public.PUBLIC, org.jooq.impl.DefaultDataType.getDefaultDataType("json"));

		setReturnParameter(RETURN_VALUE);
		addInParameter(V_INPUT);
	}

	/**
	 * Set the <code>v_input</code> parameter IN value to the routine
	 */
	public void setVInput(java.lang.String value) {
		setValue(info.nanodesu.generated.routines.ConvertToJson.V_INPUT, value);
	}

	/**
	 * Set the <code>v_input</code> parameter to the function to be used with a {@link org.jooq.Select} statement
	 */
	public void setVInput(org.jooq.Field<java.lang.String> field) {
		setField(V_INPUT, field);
	}
}
