/**
 * This class is generated by jOOQ
 */
package info.nanodesu.generated;

/**
 * This class is generated by jOOQ.
 *
 * Convenience access to all stored procedures and functions in public
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Routines {

	/**
	 * Call <code>public.convert_to_json</code>
	 */
	public static java.lang.Object convertToJson(org.jooq.Configuration configuration, java.lang.String vInput) {
		info.nanodesu.generated.routines.ConvertToJson f = new info.nanodesu.generated.routines.ConvertToJson();
		f.setVInput(vInput);

		f.execute(configuration);
		return f.getReturnValue();
	}

	/**
	 * Get <code>public.convert_to_json</code> as a field
	 */
	public static org.jooq.Field<java.lang.Object> convertToJson(java.lang.String vInput) {
		info.nanodesu.generated.routines.ConvertToJson f = new info.nanodesu.generated.routines.ConvertToJson();
		f.setVInput(vInput);

		return f.asField();
	}

	/**
	 * Get <code>public.convert_to_json</code> as a field
	 */
	public static org.jooq.Field<java.lang.Object> convertToJson(org.jooq.Field<java.lang.String> vInput) {
		info.nanodesu.generated.routines.ConvertToJson f = new info.nanodesu.generated.routines.ConvertToJson();
		f.setVInput(vInput);

		return f.asField();
	}
}
