/*
 * generated by Xtext
 */
package de.tudresden.slr.model.taxonomy.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;

public class AbstractTaxonomyValidator extends org.eclipse.xtext.validation.AbstractDeclarativeValidator {

	@Override
	protected List<EPackage> getEPackages() {
	    List<EPackage> result = new ArrayList<EPackage>();
	    result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.tudresden.de/slr/model/Taxonomy"));
		return result;
	}
}
