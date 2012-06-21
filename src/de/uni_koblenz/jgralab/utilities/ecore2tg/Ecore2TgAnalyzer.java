package de.uni_koblenz.jgralab.utilities.ecore2tg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;

public class Ecore2TgAnalyzer {

	// -------------------------------------------------------------------
	// --- Input ---------------------------------------------------------
	// -------------------------------------------------------------------

	/**
	 * Ecore metamodel
	 */
	private Resource metamodelResource;

	/**
	 * Map that saves for the EdgeClasses EReferences which overwrite which
	 */
	private HashMap<EReference, ArrayList<EReference>> ereferenceWithOverwritten = new HashMap<EReference, ArrayList<EReference>>();

	// -------------------------------------------------------------------
	// --- Output---------------------------------------------------------
	// -------------------------------------------------------------------

	/**
	 * List of all EClasses that become EdgeClasses sorted in a way, so that
	 * every superclass has an index smaller than all there subclasses
	 * */
	private ArrayList<EClass> edgeclasses;

	// --- result of getEReferences

	/**
	 * Needed for the transformation from EClasses to EdgeClasses, Containment
	 * EReferences that are not part of the Edge should not become transformed,
	 * they are ignored. The HashSet is needed during the Transformation from
	 * EClasses to EdgeClasses and later again, during the model transformation.
	 * */
	private HashSet<EReference> badEReferences;
	/**
	 * Remembers for an EdgeClass the EReferences that connect it to its alpha
	 * and omega
	 */
	private HashMap<EClass, ArrayList<EReference>> ereferencesOfEdgeClasses;

	/**
	 * Remembers for an EdgeClass whether alpha or omega is a subtype of another
	 */
	private HashMap<EClass, boolean[]> ereferencesOfEdgeClassesresult;

	// -------------------------------------------------------------------
	// --- Getter
	// -------------------------------------------------------------------

	/**
	 * @return the Ecore metamodel
	 */
	public Resource getMetamodelResource() {
		return this.metamodelResource;
	}

	public ArrayList<EClass> getFoundEdgeClasses() {
		return this.edgeclasses;
	}

	/**
	 * @return the EReferences that should be ignored during the transformation
	 */
	public HashSet<EReference> getIgnoredEReferences() {
		return this.badEReferences;
	}

	public HashMap<EClass, ArrayList<EReference>> getEreferencesOfEdgeClasses() {
		return this.ereferencesOfEdgeClasses;
	}

	public HashMap<EClass, boolean[]> getEreferencesOfEdgeClassesresult() {
		return this.ereferencesOfEdgeClassesresult;
	}

	public HashMap<EReference, ArrayList<EReference>> getEReferences2OverwrittenEReferencesMap() {
		return this.ereferenceWithOverwritten;
	}

	// -------------------------------------------------------------------
	// -------------------------------------------------------------------
	// -------------------------------------------------------------------
	// -------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param metamodel
	 */
	public Ecore2TgAnalyzer(Resource metamodel) {
		this.metamodelResource = metamodel;
	}

	// --------------------------------------------------------------------------
	// -------EdgeClasses search
	// --------------------------------------------------------------------------

	public void searchForEdgeClasses(TransformParams params,
			HashMap<EReference, ArrayList<EReference>> overwritten) {
		this.ereferenceWithOverwritten = overwritten;
		this.searchForEdgeClasses(params);
	}

	/**
	 * Looks for EClasses that seems to be EdgeClasses
	 * 
	 * @param params
	 *            Specifies whether the found EClasses are transformed into
	 *            EdgeClasses or just print to the console
	 * */
	public void searchForEdgeClasses(TransformParams params) {
		this.ereferenceWithOverwritten = new HashMap<EReference, ArrayList<EReference>>();
		this.doSearch(params);
	}

	private void doSearch(TransformParams params) {
		this.badEReferences = new HashSet<EReference>();
		this.edgeclasses = new ArrayList<EClass>();
		this.ereferencesOfEdgeClassesresult = new HashMap<EClass, boolean[]>();
		this.ereferencesOfEdgeClasses = new HashMap<EClass, ArrayList<EReference>>();

		// First find candidates, that are EClasses who have
		// 2 EReferences with the multiplicity of 1 or 3 EReferences,
		// 2 with the multiplicity of 1 and one that is a container
		ArrayList<EClass> candidates = new ArrayList<EClass>();
		this.searchCandidates(candidates);

		// Delete User Decision EdgeClasses that are still there
		candidates.removeAll(this.edgeclasses);

		ArrayList<EClass> faults = new ArrayList<EClass>();

		// Test on Pointing EReferences
		for (EClass candidate : candidates) {
			if (!this
					.checkSuperEdgeClassCandidatesOnPointingEReferences(candidate)) {
				faults.add(candidate);
			}
		}
		candidates.removeAll(faults);
		faults.clear();

		// Take the childs of that candidates
		ArrayList<EClass> childs = getSubclassesOfEClasses(
				this.metamodelResource, candidates);

		// Check if count of EReferences are not greater than 2 and Multiplicity
		// is ok
		for (EClass ec : childs) {
			if (ec.getEReferences().size() > 2) {
				// Remove the Supertype of the child from candidates
				candidates.removeAll(ec.getEAllSuperTypes());
				// Mark the child as fault and also the other Supertypes
				faults.add(ec);
				faults.addAll(ec.getEAllSuperTypes());
			} else {
				for (EReference r : ec.getEReferences()) {
					if ((r.getUpperBound() != 1) || (r.getLowerBound() != 1)) {
						// Remove the Supertype of the child from candidates
						candidates.removeAll(ec.getEAllSuperTypes());
						// Mark the child as fault and also the other Supertypes
						faults.add(ec);
						faults.addAll(ec.getEAllSuperTypes());
					}
				}
			}
		}
		childs.removeAll(faults);
		faults.clear();

		// Test on Pointing EReferences childs
		for (EClass candidate : childs) {
			boolean ok = this
					.checkSubEdgeClassCandidatesOnPointingEReferences(candidate);
			if (!ok) {
				candidates.removeAll(candidate.getEAllSuperTypes());
				faults.add(candidate);
				faults.addAll(candidate.getEAllSuperTypes());
				continue;
			}
		}
		childs.removeAll(faults);
		faults.clear();

		// Test on Compatible parents
		for (EClass candidate : childs) {
			boolean ok = this
					.checkEdgeClassCandidatesOnCompatibleEnds(candidate);
			if (!ok) {
				candidates.removeAll(candidate.getEAllSuperTypes());
				faults.add(candidate);
				faults.addAll(candidate.getEAllSuperTypes());
			}
		}
		childs.removeAll(faults);
		faults.clear();

		// Add childs to candidates
		candidates.addAll(childs);

		// Test on all parents are still there
		for (EClass candidate : candidates) {
			boolean ok = candidates.containsAll(candidate.getEAllSuperTypes());
			if (!ok) {
				faults.add(candidate);
				faults.addAll(candidate.getEAllSuperTypes());
			}
		}
		candidates.removeAll(faults);
		faults.clear();

		// Test on valid endpoints
		for (EClass candidate : candidates) {
			boolean ok = this.checkEdgeClassCandidatesOnValidEndpoints(
					candidate, candidates, this.edgeclasses);
			if (!ok) {
				faults.add(candidate);
				faults.addAll(candidate.getEAllSuperTypes());
			}
		}
		candidates.removeAll(faults);
		faults.clear();

		// Again test on all parents
		for (EClass candidate : candidates) {
			boolean ok = candidates.containsAll(candidate.getEAllSuperTypes());
			if (!ok) {
				faults.add(candidate);
				faults.addAll(candidate.getEAllSuperTypes());
			}
		}
		candidates.removeAll(faults);
		faults.clear();

		if (params == TransformParams.AUTOMATIC_TRANSFORMATION) {
			// Now write the resulting candidates into the edgeclasses list
			for (int x = 0; x < candidates.size(); x++) {
				if (candidates.get(x) != null) {
					this.edgeclasses.add(candidates.get(x));
				}
			}
		}
		// else{
		if (candidates.size() != 0) {
			System.out.println("   EdgeClass candidates are: ");
			for (int x = 0; x < candidates.size(); x++) {
				if (candidates.get(x) != null) {
					System.out.println("   - "
							+ getQualifiedEClassName(candidates.get(x)));
				}
			}
		}
		sortEClasses(this.edgeclasses);
	}

	/**
	 * Method that searches candidates for EdgeClasses Candidates are EClasses
	 * with 2 EReferences that have the multiplicity of 1 or EClasses with 0
	 * EReferences
	 * 
	 * @param candidates
	 *            ArrayList with all yet found candidates
	 * */
	private void searchCandidates(ArrayList<EClass> candidates) {
		for (EObject ob : this.metamodelResource.getContents()) {
			this.searchCandidates(candidates, (EPackage) ob);
		}
	}

	/**
	 * Method that searches candidates for EdgeClasses in the Resource.
	 * Candidates are EClasses with 2 EReferences that have the multiplicity of
	 * 1 or EClasses with 0 EReferences
	 * 
	 * @param candidates
	 *            ArrayList with all yet found candidates
	 * @param pack
	 *            EPackage to search in
	 * */
	private void searchCandidates(ArrayList<EClass> candidates, EPackage pack) {
		for (EClassifier classi : pack.getEClassifiers()) {
			// if it is an EClass
			if (classi instanceof EClass) {
				EClass testclass = (EClass) classi;
				// test the number of EReferences
				if (testclass.getESuperTypes().size() > 0) {
					// Test only super types
					continue;
				}
				if (testclass.getEReferences().size() == 2) {
					boolean isok = true;
					for (EReference eref : testclass.getEReferences()) {
						if ((eref.getLowerBound() != 1)
								|| (eref.getUpperBound() != 1)) {
							isok = false;
						}
					}
					if (isok) {
						candidates.add(testclass);
					}
				} else if (testclass.getEReferences().size() == 3) {
					int cc = -1;
					for (int i = 0; i < testclass.getEReferences().size(); i++) {
						if (testclass.getEReferences().get(i).isContainer()) {
							cc = i;
						}
					}
					if (cc >= 0) {
						EReference one = testclass.getEReferences().get(
								(cc + 1) % 3);
						EReference two = testclass.getEReferences().get(
								(cc + 2) % 3);
						if ((one.getLowerBound() == 1)
								&& (one.getUpperBound() == 1)
								&& (two.getLowerBound() == 1)
								&& (two.getUpperBound() == 1)
								&& !one.isContainment() && !two.isContainment()) {
							candidates.add(testclass);
						}
					}
				}
			}
		}
		// Search in all subpackages
		for (EPackage childpack : pack.getESubpackages()) {
			this.searchCandidates(candidates, childpack);
		}
	}

	/**
	 * Test if the EReferences that points on an EClass are valid for an
	 * EdgeClass
	 * 
	 * @param candidate
	 *            EClass with no super types to test
	 * @return whether the candidate passes the pointing EReference test
	 * */
	private boolean checkSuperEdgeClassCandidatesOnPointingEReferences(
			EClass candidate) {
		ArrayList<EReference> erefs = new ArrayList<EReference>();
		getEReferences_that_point_on_EClass(this.metamodelResource, candidate,
				erefs);
		// 3 Pointing EReferences
		if (erefs.size() == 3) {
			// 2 own EReferences
			if (candidate.getEReferences().size() == 2) {
				if (erefs.contains(candidate.getEReferences().get(0)
						.getEOpposite())
						&& erefs.contains(candidate.getEReferences().get(1)
								.getEOpposite())) {
					erefs.remove(candidate.getEReferences().get(0)
							.getEOpposite());
					erefs.remove(candidate.getEReferences().get(1)
							.getEOpposite());
					if (!erefs.get(0).isContainment()) {
						return false;
					}
					if (candidate.getEReferences().get(0).isContainment()
							|| candidate.getEReferences().get(1)
									.isContainment()
							|| candidate.getEReferences().get(0).isContainer()
							|| candidate.getEReferences().get(1).isContainer()) {
						return false;
					}
				} else {
					return false;
				}
			}
			// 3 own EReferences
			else if (candidate.getEReferences().size() == 3) {
				if (erefs.contains(candidate.getEReferences().get(0)
						.getEOpposite())
						&& erefs.contains(candidate.getEReferences().get(1)
								.getEOpposite())
						&& erefs.contains(candidate.getEReferences().get(2)
								.getEOpposite())) {
					if (erefs.get(0).isContainment()) {
						erefs.remove(0);
					}
					if (erefs.get(1).isContainment()) {
						erefs.remove(1);
					}
					if (erefs.get(2).isContainment()) {
						erefs.remove(2);
					}
					if (erefs.size() != 2) {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		// 2 EReferences pointing
		else if (erefs.size() == 2) {
			EReference ref1 = erefs.get(0);
			EReference ref2 = erefs.get(1);
			// 2 own EReferences
			if (candidate.getEReferences().size() == 2) {
				if ((ref1.getEOpposite() != null)
						&& candidate.getEReferences().contains(
								ref1.getEOpposite())
						&& (ref2.getEOpposite() != null)
						&& candidate.getEReferences().contains(
								ref2.getEOpposite())) {
					// []---[]---[]
					if ((ref1.isContainment() != ref2.getEOpposite()
							.isContainment())
							|| (ref2.isContainment() != ref1.getEOpposite()
									.isContainment())) {
						return false;
					}
				} else if (((ref1.getEOpposite() != null)
						&& candidate.getEReferences().contains(
								ref1.getEOpposite()) && ref2.isContainment())
						|| ((ref2.getEOpposite() != null)
								&& candidate.getEReferences().contains(
										ref2.getEOpposite()) && ref1
									.isContainment())) {
					// []
					// []--[]--[]
					if (candidate.getEReferences().get(0).isContainment()
							|| candidate.getEReferences().get(1)
									.isContainment()
							|| candidate.getEReferences().get(0).isContainer()
							|| candidate.getEReferences().get(1).isContainer()) {
						return false;
					}
				} else {
					return false;
				}
			}
			// 3 own EReferences
			else if (candidate.getEReferences().size() == 3) {
				if ((ref1.getEOpposite() == null)
						|| !candidate.getEReferences().contains(
								ref1.getEOpposite())
						|| (ref2.getEOpposite() == null)
						|| !candidate.getEReferences().contains(
								ref2.getEOpposite())) {
					return false;
				}
			}
		}
		// 1 pointing EReference
		else if (erefs.size() == 1) {
			EReference r = erefs.get(0);
			if ((candidate.getEReferences().size() != 2)
					|| (r.getEOpposite() == null)
					|| !candidate.getEReferences().contains(r.getEOpposite())) {
				return false;
			}
			if ((r.getEOpposite() != candidate.getEReferences().get(0))
					&& (r.isContainment() != candidate.getEReferences().get(0)
							.isContainment())) {
				return false;
			}
			if ((r.getEOpposite() != candidate.getEReferences().get(1))
					&& (r.isContainment() != candidate.getEReferences().get(1)
							.isContainment())) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Test if the EReferences that points on an EClass are valid for an
	 * EdgeClass
	 * 
	 * @param candidate
	 *            EClass with super types to test
	 * @return whether the candidate passes the pointing EReference test
	 * */
	private boolean checkSubEdgeClassCandidatesOnPointingEReferences(
			EClass candidate) {

		// Save all EReferences that references the candidate
		ArrayList<EReference> erefs_that_point_on_candidate = new ArrayList<EReference>();
		getEReferences_that_point_on_EClass(this.metamodelResource, candidate,
				erefs_that_point_on_candidate);

		// Save all EReferences that belongs to the candidate
		EList<EReference> erefs_from_candidate = candidate.getEReferences();

		// No own EReferences
		if (erefs_from_candidate.size() == 0) {
			// Only ok, if there are no pointing ones
			if (erefs_that_point_on_candidate.size() != 0) {
				return false;
			}
		}
		// 1 own EReference
		else if (erefs_from_candidate.size() == 1) {
			// []--->
			if (erefs_that_point_on_candidate.size() == 0) {
				return true;
			} else if (erefs_that_point_on_candidate.size() == 1) {
				// []----
				if ((erefs_that_point_on_candidate.get(0).getEOpposite() != null)
						&& (erefs_from_candidate.get(0) == erefs_that_point_on_candidate
								.get(0).getEOpposite())) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		// 2 own EReferences
		else if (erefs_from_candidate.size() == 2) {
			if (erefs_that_point_on_candidate.size() == 1) {
				// []---
				// --->
				if ((erefs_that_point_on_candidate.get(0).getEOpposite() != null)
						&& ((erefs_that_point_on_candidate.get(0)
								.getEOpposite() == erefs_from_candidate.get(0)) || (erefs_that_point_on_candidate
								.get(0).getEOpposite() == erefs_from_candidate
								.get(1)))) {
					return true;
				} else {
					return false;
				}
			} else if (erefs_that_point_on_candidate.size() == 2) {
				EReference opp1 = erefs_that_point_on_candidate.get(0)
						.getEOpposite();
				EReference opp2 = erefs_that_point_on_candidate.get(1)
						.getEOpposite();
				// []---
				// ----
				if (((erefs_from_candidate.get(0) == opp1) && (erefs_from_candidate
						.get(1) == opp2))
						|| ((erefs_from_candidate.get(0) == opp2) && (erefs_from_candidate
								.get(1) == opp1))) {
					return true;
				} else {
					return false;
				}
			}
			// More than 2 pointing refs
			else {
				return false;
			}
		}
		// more than 2 own EReferences - not possible
		else {
			return false;
		}

		return true;
	}

	/**
	 * Test if the EClasses that the suppose-to-be EdgeClass connects are
	 * distinctly compatible to the EClasses all parents of the candidate
	 * connects
	 * 
	 * @param candidate
	 *            EClass with super types to test
	 * @return whether the candidate passes the parent compatibility test
	 * */
	private boolean checkEdgeClassCandidatesOnCompatibleEnds(EClass candidate) {

		// Get EReferences to test
		ArrayList<EReference> resultlist = new ArrayList<EReference>();
		getEdgesEReferences(this.metamodelResource, candidate,
				this.ereferenceWithOverwritten, resultlist,
				this.badEReferences, this.ereferencesOfEdgeClasses,
				this.ereferencesOfEdgeClassesresult);
		EReference erefFromEdgeToEClass1 = resultlist.get(0);
		EReference erefFromEClass1ToEdge = resultlist.get(1);
		EReference erefFromEdgeToEClass2 = resultlist.get(2);
		EReference erefFromEClass2ToEdge = resultlist.get(3);

		// EdgeClasses of Child
		EClass eclass1;
		EClass eclass2;
		if (erefFromEdgeToEClass1 != null) {
			eclass1 = erefFromEdgeToEClass1.getEReferenceType();
		} else {
			eclass1 = erefFromEClass1ToEdge.getEContainingClass();
		}

		if (erefFromEdgeToEClass2 != null) {
			eclass2 = erefFromEdgeToEClass2.getEReferenceType();
		} else {
			eclass2 = erefFromEClass2ToEdge.getEContainingClass();
		}

		// Iterate over all parents
		for (EClass parent : candidate.getEAllSuperTypes()) {
			resultlist.clear();
			getEdgesEReferences(this.metamodelResource, parent,
					this.ereferenceWithOverwritten, resultlist,
					this.badEReferences, this.ereferencesOfEdgeClasses,
					this.ereferencesOfEdgeClassesresult);
			EReference erefParentFromEdgeToEClass1 = resultlist.get(0);
			EReference erefParentFromEClass1ToEdge = resultlist.get(1);
			EReference erefParentFromEdgeToEClass2 = resultlist.get(2);
			EReference erefParentFromEClass2ToEdge = resultlist.get(3);

			EClass eclass1Parent;
			EClass eclass2Parent;
			if (erefParentFromEdgeToEClass1 != null) {
				eclass1Parent = erefParentFromEdgeToEClass1.getEReferenceType();
			} else {
				eclass1Parent = erefParentFromEClass1ToEdge
						.getEContainingClass();
			}

			if (erefParentFromEdgeToEClass2 != null) {
				eclass2Parent = erefParentFromEdgeToEClass2.getEReferenceType();
			} else {
				eclass2Parent = erefParentFromEClass2ToEdge
						.getEContainingClass();
			}

			// Test on Compatibility of EndClasses
			if (((eclass1 == eclass1Parent) || eclass1.getEAllSuperTypes()
					.contains(eclass1Parent))
					&& ((eclass2 == eclass2Parent) || eclass2
							.getEAllSuperTypes().contains(eclass2Parent))) {
				// compatible - equal?
				if ((eclass1 == eclass2Parent)
						|| (eclass1.getEAllSuperTypes().contains(eclass2Parent) && ((eclass2 == eclass1Parent) || eclass2
								.getEAllSuperTypes().contains(eclass1Parent)))) {
					// both directions are compatible - if the EReferences are
					// equal, no problem
					if (!(erefFromEClass1ToEdge.getName().equals(
							erefParentFromEClass1ToEdge.getName())
							&& erefFromEClass2ToEdge.getName().equals(
									erefParentFromEClass2ToEdge.getName())
							&& erefFromEdgeToEClass1.getName().equals(
									erefParentFromEdgeToEClass1.getName()) && erefFromEdgeToEClass2
							.getName().equals(
									erefParentFromEdgeToEClass2.getName()))
							&& !(erefFromEClass1ToEdge.getName().equals(
									erefParentFromEClass2ToEdge.getName())
									&& erefFromEClass2ToEdge.getName().equals(
											erefParentFromEClass1ToEdge
													.getName())
									&& erefFromEdgeToEClass1.getName().equals(
											erefParentFromEdgeToEClass2
													.getName()) && erefFromEdgeToEClass2
									.getName().equals(
											erefParentFromEdgeToEClass1
													.getName()))) {
						System.out
								.println("The EClass "
										+ getQualifiedEClassName(candidate)
										+ " is probably an EdgeClass, but it is not possible to determine which EReference overwrites the parents one.");
						return false;
					}
				}
				// else is ok
				else {
					// Test on Containment Equality
					if ((erefFromEClass1ToEdge != null)
							&& (erefParentFromEClass1ToEdge != null)
							&& (erefFromEClass1ToEdge.isContainment() != erefParentFromEClass1ToEdge
									.isContainment())) {
						return false;
					}
					if ((erefFromEClass2ToEdge != null)
							&& (erefParentFromEClass2ToEdge != null)
							&& (erefFromEClass2ToEdge.isContainment() != erefParentFromEClass2ToEdge
									.isContainment())) {
						return false;
					}
					if ((erefFromEdgeToEClass1 != null)
							&& (erefParentFromEdgeToEClass1 != null)
							&& (erefFromEdgeToEClass1.isContainment() != erefParentFromEdgeToEClass1
									.isContainment())) {
						return false;
					}
					if ((erefFromEdgeToEClass2 != null)
							&& (erefParentFromEdgeToEClass2 != null)
							&& (erefFromEdgeToEClass2.isContainment() != erefParentFromEdgeToEClass2
									.isContainment())) {
						return false;
					}
				}
			} else if ((eclass1 == eclass2Parent)
					|| (eclass1.getEAllSuperTypes().contains(eclass2Parent) && ((eclass2 == eclass1Parent) || eclass2
							.getEAllSuperTypes().contains(eclass1Parent)))) {
				// ok - equal too is not possible, than the first would be
				// chosen
				// Test on Containment ok
				if ((erefFromEClass1ToEdge != null)
						&& (erefParentFromEClass2ToEdge != null)
						&& (erefFromEClass1ToEdge.isContainment() != erefParentFromEClass2ToEdge
								.isContainment())) {
					return false;
				}
				if ((erefFromEClass2ToEdge != null)
						&& (erefParentFromEClass1ToEdge != null)
						&& (erefFromEClass2ToEdge.isContainment() != erefParentFromEClass1ToEdge
								.isContainment())) {
					return false;
				}
				if ((erefFromEdgeToEClass1 != null)
						&& (erefParentFromEdgeToEClass2 != null)
						&& (erefFromEdgeToEClass1.isContainment() != erefParentFromEdgeToEClass2
								.isContainment())) {
					return false;
				}
				if ((erefFromEdgeToEClass2 != null)
						&& (erefParentFromEdgeToEClass1 != null)
						&& (erefFromEdgeToEClass2.isContainment() != erefParentFromEdgeToEClass1
								.isContainment())) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}

	/**
	 * Test if the EClasses that the suppose-to-be EdgeClass connects are no
	 * candidates for EdgeClasses
	 * 
	 * @param candidate
	 *            EClass to test
	 * @return whether the candidate passes the valid end point test
	 * */
	private boolean checkEdgeClassCandidatesOnValidEndpoints(EClass candidate,
			ArrayList<EClass> candidates, ArrayList<EClass> edgeclasses) {
		// Get EReferences to test
		ArrayList<EReference> resultlist = new ArrayList<EReference>();
		getEdgesEReferences(this.metamodelResource, candidate,
				this.ereferenceWithOverwritten, resultlist,
				this.badEReferences, this.ereferencesOfEdgeClasses,
				this.ereferencesOfEdgeClassesresult);
		EReference erefFromEdgeToEClass1 = resultlist.get(0);
		EReference erefFromEClass1ToEdge = resultlist.get(1);
		EReference erefFromEdgeToEClass2 = resultlist.get(2);
		EReference erefFromEClass2ToEdge = resultlist.get(3);

		// EdgeClasses of Child
		EClass eclass1;
		EClass eclass2;
		if (erefFromEdgeToEClass1 != null) {
			eclass1 = erefFromEdgeToEClass1.getEReferenceType();
		} else {
			eclass1 = erefFromEClass1ToEdge.getEContainingClass();
		}

		if (erefFromEdgeToEClass2 != null) {
			eclass2 = erefFromEdgeToEClass2.getEReferenceType();
		} else {
			eclass2 = erefFromEClass2ToEdge.getEContainingClass();
		}

		if (candidates.contains(eclass1) || candidates.contains(eclass2)) {
			return false;
		}
		if (edgeclasses.contains(eclass1) || edgeclasses.contains(eclass2)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Finds the EReferences for an EClass that should become transformed to an
	 * EdgeClass which have influence on the incidences
	 * 
	 * @param candidate
	 *            EClass that is EdgeClass
	 * @param resultlist
	 *            empty list to put the results in
	 * 
	 * @return if the two EReferences pairs are inherited from a super type of
	 *         candidate
	 * */
	public static boolean[] getEdgesEReferences(
			Resource metamodel,
			EClass candidate,
			HashMap<EReference, ArrayList<EReference>> ereferenceWithOverwritten,
			ArrayList<EReference> resultlist,
			HashSet<EReference> badEReferences,
			HashMap<EClass, ArrayList<EReference>> ereferencesOfEdgeClasses,
			HashMap<EClass, boolean[]> ereferencesOfEdgeClassesresult) {
		if (ereferencesOfEdgeClasses.get(candidate) != null) {
			resultlist.addAll(ereferencesOfEdgeClasses.get(candidate));
			return ereferencesOfEdgeClassesresult.get(candidate);
		}
		boolean[] x = { false, false };
		if (candidate.getESuperTypes().size() == 0) {
			getEdgesEReferencesForMostSupertype(metamodel, candidate,
					resultlist, badEReferences);
		} else {
			x = getEdgesEReferencesForSubtypes(metamodel, candidate,
					resultlist, badEReferences, ereferenceWithOverwritten);
		}
		ArrayList<EReference> templist = new ArrayList<EReference>();
		templist.addAll(resultlist);
		ereferencesOfEdgeClasses.put(candidate, templist);
		ereferencesOfEdgeClassesresult.put(candidate, x);
		return x;
	}

	/**
	 * Finds the EReferences for an EClass that should become transformed to an
	 * EdgeClass which have influence on the incidences - Version for candidates
	 * without super types
	 * 
	 * @param candidate
	 *            EClass that is EdgeClass
	 * @param resultlist
	 *            empty list to put the results in
	 * */
	private static void getEdgesEReferencesForMostSupertype(
			Resource metamodelResource, EClass candidate,
			ArrayList<EReference> resultlist, HashSet<EReference> badEReferences) {

		EReference toEnd1 = null;
		EReference fromEnd1 = null;
		EReference toEnd2 = null;
		EReference fromEnd2 = null;
		EReference toCont = null;
		EReference fromCont = null;

		ArrayList<EReference> ownRefs = new ArrayList<EReference>();
		ownRefs.addAll(candidate.getEReferences());
		// Exclude refs to RecordDomains
		ArrayList<EReference> toDelete = new ArrayList<EReference>();
		for (EReference ed : ownRefs) {
			if ((ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_REF_TO_RECORD)) {
				toDelete.add(ed);
			}
		}
		ownRefs.removeAll(toDelete);

		ArrayList<EReference> foreignRefs = new ArrayList<EReference>();
		Ecore2TgAnalyzer.getEReferences_that_point_on_EClass(metamodelResource,
				candidate, foreignRefs);

		// There are two different Classes plus the container - super
		if (ownRefs.size() == 3) {
			if (ownRefs.get(0).isContainer()) {
				toEnd1 = ownRefs.get(1);
				fromEnd1 = toEnd1.getEOpposite();
				toEnd2 = ownRefs.get(2);
				fromEnd2 = toEnd2.getEOpposite();
				toCont = ownRefs.get(0);
				fromCont = toCont.getEOpposite();
			} else if (ownRefs.get(1).isContainer()) {
				toEnd1 = ownRefs.get(0);
				fromEnd1 = toEnd1.getEOpposite();
				toEnd2 = ownRefs.get(2);
				fromEnd2 = toEnd2.getEOpposite();
				toCont = ownRefs.get(1);
				fromCont = toCont.getEOpposite();
			} else if (ownRefs.get(2).isContainer()) {
				toEnd1 = ownRefs.get(0);
				fromEnd1 = toEnd1.getEOpposite();
				toEnd2 = ownRefs.get(1);
				fromEnd2 = toEnd2.getEOpposite();
				toCont = ownRefs.get(2);
				fromCont = toCont.getEOpposite();
			}
		}
		// Many cases
		else if (ownRefs.size() == 2) {
			if (!ownRefs.get(0).isContainer() && !ownRefs.get(1).isContainer()) {
				toEnd1 = ownRefs.get(0);
				fromEnd1 = toEnd1.getEOpposite();
				toEnd2 = ownRefs.get(1);
				fromEnd2 = toEnd2.getEOpposite();
				foreignRefs.remove(fromEnd1);
				foreignRefs.remove(fromEnd2);
				if (foreignRefs.size() > 0) {
					fromCont = foreignRefs.get(0);
					toCont = fromCont.getEOpposite();
				}
			} else if (ownRefs.get(0).isContainer()) {
				EReference toX = ownRefs.get(0);
				toEnd2 = ownRefs.get(1);
				fromEnd2 = toEnd2.getEOpposite();
				foreignRefs.remove(toX.getEOpposite());
				foreignRefs.remove(fromEnd2);
				if (foreignRefs.size() > 0) {
					fromEnd1 = foreignRefs.get(0);
					toEnd1 = fromEnd1.getEOpposite();
					toCont = toX;
					fromCont = toCont.getEOpposite();
				} else {
					toEnd1 = toX;
					fromEnd1 = toX.getEOpposite();
				}
			} else if (ownRefs.get(1).isContainer()) {
				EReference toX = ownRefs.get(1);
				toEnd2 = ownRefs.get(0);
				fromEnd2 = toEnd2.getEOpposite();
				foreignRefs.remove(toX.getEOpposite());
				foreignRefs.remove(fromEnd2);
				if (foreignRefs.size() > 0) {
					fromEnd1 = foreignRefs.get(0);
					toEnd1 = fromEnd1.getEOpposite();
					toCont = toX;
					fromCont = toX.getEOpposite();
				} else {
					toEnd1 = toX;
					fromEnd1 = toX.getEOpposite();
				}
			}
		}
		// There is no ERef to Containment
		else if (ownRefs.size() == 1) {
			toEnd1 = ownRefs.get(0);
			fromEnd1 = toEnd1.getEOpposite();
			foreignRefs.remove(fromEnd1);
			if (foreignRefs.size() == 1) {
				fromEnd2 = foreignRefs.get(0);
				toEnd2 = fromEnd2.getEOpposite();
			} else if (foreignRefs.size() == 2) {
				if (fromEnd1 != null) {
					if (foreignRefs.get(0) == fromEnd1) {
						fromEnd2 = foreignRefs.get(1);
					} else {
						fromEnd2 = foreignRefs.get(0);
					}
				} else {
					if (foreignRefs.get(0).isContainment()) {
						fromEnd2 = foreignRefs.get(1);
						toEnd2 = fromEnd2.getEOpposite();
						fromCont = foreignRefs.get(0);
						toCont = fromCont.getEOpposite();
					} else {
						fromEnd2 = foreignRefs.get(0);
						toEnd2 = fromEnd2.getEOpposite();
						fromCont = foreignRefs.get(1);
						toCont = fromCont.getEOpposite();
					}
				}
			} else if (foreignRefs.size() == 3) {
				if (foreignRefs.get(0).isContainment()) {
					fromCont = foreignRefs.get(0);
					if (foreignRefs.get(1) == fromEnd1) {
						fromEnd2 = foreignRefs.get(2);
					} else {
						fromEnd2 = foreignRefs.get(1);
					}
				} else if (foreignRefs.get(1).isContainment()) {
					fromCont = foreignRefs.get(1);
					if (foreignRefs.get(0) == fromEnd1) {
						fromEnd2 = foreignRefs.get(2);
					} else {
						fromEnd2 = foreignRefs.get(0);
					}
				} else if (foreignRefs.get(2).isContainment()) {
					fromCont = foreignRefs.get(2);
					if (foreignRefs.get(0) == fromEnd1) {
						fromEnd2 = foreignRefs.get(1);
					} else {
						fromEnd2 = foreignRefs.get(0);
					}
				}
			} else {
				System.out.println("Error - should not appear here");
			}
		}

		resultlist.add(toEnd1);
		resultlist.add(fromEnd1);
		resultlist.add(toEnd2);
		resultlist.add(fromEnd2);

		badEReferences.add(fromCont);
		badEReferences.add(toCont);

		// Remember Containment EReferences to EdgeClasses to get the original
		// Ecore Metamodel back, during a following Tg2Ecore transform
		// Not generated by Tg2Ecore
		resultlist.add(toCont);
		resultlist.add(fromCont);

	}

	/**
	 * Finds the EReferences for an EClass that should become transformed to an
	 * EdgeClass which have influence on the incidences - Version for candidates
	 * with super types
	 * 
	 * @param candidate
	 *            EClass that is EdgeClass
	 * @param resultlist
	 *            empty list to put the results in
	 * 
	 * @return if the two EReferences pairs are inherited from a super type of
	 *         candidate
	 * */
	private static boolean[] getEdgesEReferencesForSubtypes(
			Resource metamodelResource, EClass candidate,
			ArrayList<EReference> resultlist,
			HashSet<EReference> badEReferences,
			HashMap<EReference, ArrayList<EReference>> ereferenceWithOverwritten) {

		ArrayList<EReference> ownRefs = new ArrayList<EReference>();
		ArrayList<EReference> pointingRefs = new ArrayList<EReference>();

		ownRefs.addAll(candidate.getEAllReferences());
		// Exclude refs to RecordDomains
		for (EReference ed : ownRefs) {
			if ((ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_REF_TO_RECORD)) {
				ownRefs.remove(ed);
			}
		}
		Ecore2TgAnalyzer.getEReferences_that_point_on_EClass(metamodelResource,
				candidate, pointingRefs);
		for (EClass parent : candidate.getEAllSuperTypes()) {
			Ecore2TgAnalyzer.getEReferences_that_point_on_EClass(
					metamodelResource, parent, pointingRefs);
		}

		ownRefs.removeAll(badEReferences);
		pointingRefs.removeAll(badEReferences);

		ArrayList<EClass> endpoints = new ArrayList<EClass>();
		ArrayList<EReference> refsOfEndpoints = new ArrayList<EReference>();
		getEndpoints(candidate, ownRefs, pointingRefs, endpoints,
				refsOfEndpoints);

		EClass end1 = null;
		EClass end2 = null;

		EReference toEnd1 = null;
		EReference fromEnd1 = null;
		EReference toEnd2 = null;
		EReference fromEnd2 = null;

		EClass targetEdge = null;

		// Problemcase - directions, inheritance, badbad
		// Only one Endpoint-Hierarchy
		if (endpoints.size() == 1) {
			end1 = endpoints.get(0);
			EReference temp1 = refsOfEndpoints.get(0);

			if (temp1.getEReferenceType() == end1) {
				toEnd1 = temp1;
				targetEdge = temp1.getEContainingClass();
				if (toEnd1.getEOpposite() != null) {
					fromEnd1 = toEnd1.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen????
				}
			} else { // temp1.getEContainingClass() == end1
				fromEnd1 = temp1;
				targetEdge = temp1.getEReferenceType();
				if (fromEnd1.getEOpposite() != null) {
					toEnd1 = fromEnd1.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen?
				}
			}

			// Look for other EReferences between targetEdge and end1
			EReference refFromEnd1ToTargetEdge = null;
			for (EReference testRef : end1.getEReferences()) {
				if ((testRef.getEReferenceType() == targetEdge)
						&& (testRef != fromEnd1)) {
					refFromEnd1ToTargetEdge = testRef;
				}
			}

			EReference refFromTargetEdgeToEnd1 = null;
			for (EReference testRef : targetEdge.getEReferences()) {
				if ((testRef.getEReferenceType() == end1)
						&& (testRef != toEnd1)) {
					refFromTargetEdgeToEnd1 = testRef;
				}
			}

			// Between the found TargetClass and the Endpoint are enough
			// EReferences
			// to create an EdgeClass of
			if ((refFromEnd1ToTargetEdge != null)
					|| (refFromTargetEdgeToEnd1 != null)) {
				if (refFromEnd1ToTargetEdge != null) {
					fromEnd2 = refFromEnd1ToTargetEdge;
					toEnd2 = fromEnd2.getEOpposite();
					end2 = fromEnd2.getEContainingClass();
				} else {
					toEnd2 = refFromTargetEdgeToEnd1;
					fromEnd2 = toEnd2.getEOpposite();
					end2 = toEnd2.getEReferenceType();
				}
			}
			// Only one connection from TargetEdge to end1 is found,
			// the other connection must be to a superclass
			else {
				ownRefs.remove(toEnd1);
				pointingRefs.remove(fromEnd1);
				if (ereferenceWithOverwritten.get(toEnd1) != null) {
					ownRefs.removeAll(ereferenceWithOverwritten.get(toEnd1));
				}
				if (ereferenceWithOverwritten.get(fromEnd1) != null) {
					pointingRefs.removeAll(ereferenceWithOverwritten
							.get(fromEnd1));
				}
				ArrayList<EReference> result = new ArrayList<EReference>();
				findSecondEndpoint(candidate, end1, toEnd1, fromEnd1, ownRefs,
						pointingRefs, result, ereferenceWithOverwritten);
				toEnd2 = result.get(0);
				fromEnd2 = result.get(1);
				if (toEnd2 != null) {
					end2 = toEnd2.getEReferenceType();
				} else {
					end2 = fromEnd2.getEContainingClass();
				}
			}
		}

		// Ok case - there are two independent endpoints
		else if (endpoints.size() == 2) {
			end1 = endpoints.get(0);
			end2 = endpoints.get(1);

			EReference temp1 = refsOfEndpoints.get(0);
			EReference temp2 = refsOfEndpoints.get(1);

			if (temp1.getEReferenceType() == end1) {
				toEnd1 = temp1;
				if (toEnd1.getEOpposite() != null) {
					fromEnd1 = toEnd1.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen????
				}
			} else { // tem1.getEContainingClass() == end1
				fromEnd1 = temp1;
				if (fromEnd1.getEOpposite() != null) {
					toEnd1 = fromEnd1.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen?
				}
			}

			if (temp2.getEReferenceType() == end2) {
				toEnd2 = temp2;
				if (toEnd2.getEOpposite() != null) {
					fromEnd2 = toEnd2.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen????
				}
			} else { // temp2.getEContainingClass() == end2
				fromEnd2 = temp2;
				if (fromEnd2.getEOpposite() != null) {
					toEnd2 = fromEnd2.getEOpposite();
				} else {
					// Muss ich jetzt weiter oben suchen?
				}
			}
		} else {
			System.out.println("Help - that should never happen");
		}

		resultlist.add(toEnd1);
		resultlist.add(fromEnd1);
		resultlist.add(toEnd2);
		resultlist.add(fromEnd2);

		boolean subtypes[] = new boolean[2];
		subtypes[0] = true;
		subtypes[1] = true;
		pointingRefs.clear();
		Ecore2TgAnalyzer.getEReferences_that_point_on_EClass(metamodelResource,
				candidate, pointingRefs);
		if (candidate.getEReferences().contains(toEnd1)
				|| pointingRefs.contains(fromEnd1)) {
			subtypes[0] = false;
		}
		if (candidate.getEReferences().contains(toEnd2)
				|| pointingRefs.contains(fromEnd2)) {
			subtypes[1] = false;
		}

		return subtypes;
	}

	/**
	 * Finds the end point EClasses of an suppose-to-be EdgeClass Works through
	 * all EReferences of the candidate and all EReferences that reference the
	 * candidate or a parent. From that it collects the other ending of the
	 * EReference. That Collection is then examined on super types. The result
	 * are two end points if two independent trees are found and one if there is
	 * only one.
	 * 
	 * @param candidate
	 *            the EClass that is an EdgeClass
	 * @param ownRefs
	 *            the EReferences the candidate has
	 * @param pointingRefs
	 *            the EReferences that point on the candidate
	 * 
	 * @param endpoints
	 *            resulting end points
	 * @param refsOfEndpoints
	 *            references that lead to the resulting end points
	 * */
	private static void getEndpoints(EClass candidate,
			ArrayList<EReference> ownRefs, ArrayList<EReference> pointingRefs,
			ArrayList<EClass> endpoints, ArrayList<EReference> refsOfEndpoints) {

		for (EReference er : ownRefs) {
			EClass c = er.getEReferenceType();
			boolean notIn = true;

			for (EClass endpoint : endpoints) {
				if (c.getEAllSuperTypes().contains(endpoint)) {
					int i = endpoints.indexOf(endpoint);
					endpoints.set(i, c);
					refsOfEndpoints.set(i, er);
					notIn = false;
				} else if (endpoint.getEAllSuperTypes().contains(c)) {
					notIn = false;
				} else if (endpoint == c) {
					if (er.getEContainingClass()
							.getEAllSuperTypes()
							.contains(
									refsOfEndpoints.get(
											endpoints.indexOf(endpoint))
											.getEContainingClass())) {
						refsOfEndpoints.set(endpoints.indexOf(endpoint), er);
					}
					notIn = false;
				}
			}
			if (notIn) {
				endpoints.add(c);
				refsOfEndpoints.add(er);
			}
		}

		for (EReference er : pointingRefs) {
			EClass c = er.getEContainingClass();
			boolean notIn = true;
			for (EClass endpoint : endpoints) {
				if (c.getEAllSuperTypes().contains(endpoint)) {
					int i = endpoints.indexOf(endpoint);
					endpoints.set(i, c);
					refsOfEndpoints.set(i, er);
					notIn = false;
				} else if (endpoint.getEAllSuperTypes().contains(c)) {
					notIn = false;
				} else if (endpoint == c) {
					if (er.getEReferenceType()
							.getEAllSuperTypes()
							.contains(
									refsOfEndpoints.get(
											endpoints.indexOf(endpoint))
											.getEReferenceType())) {
						refsOfEndpoints.set(endpoints.indexOf(endpoint), er);
					}
					notIn = false;
				}
			}
			if (notIn) {
				endpoints.add(c);
				refsOfEndpoints.add(er);
			}
		}
	}

	/**
	 * Finds the second end point if
	 * {@link #getEndpoints(EClass, ArrayList, ArrayList, ArrayList, ArrayList)}
	 * has found only one.
	 * 
	 * @param candidate
	 *            EClass to find the end point for
	 * @param end1
	 *            first found end point
	 * @param toEnd1
	 *            EReference from candidate or a super type of candidate to the
	 *            end point 1 if exists
	 * @param fromEnd1
	 *            EReference from end point 1 to candidate or a super type of
	 *            candidate if exists
	 * @param ownRefs
	 *            list of EReferences of the candidate
	 * @param pointingRefs
	 *            list of EReferences pointing on candidate
	 * @param endpi
	 * */
	private static void findSecondEndpoint(EClass candidate, EClass end1,
			EReference toEnd1, EReference fromEnd1,
			ArrayList<EReference> ownRefs, ArrayList<EReference> pointingRefs,
			ArrayList<EReference> result,
			HashMap<EReference, ArrayList<EReference>> ereferenceWithOverwritten) {

		ArrayList<EClass> endpoints = new ArrayList<EClass>();
		ArrayList<EReference> refsOfEndpoints = new ArrayList<EReference>();

		EReference toEnd2 = null;
		EReference fromEnd2 = null;

		getEndpoints(candidate, ownRefs, pointingRefs, endpoints,
				refsOfEndpoints);
		// The last time only one endpoint was found -
		// this time it should be the same
		if (endpoints.size() == 1) {
			EClass prob2 = endpoints.get(0);
			EClass tc;
			EReference toProb2;
			EReference fromProb2;

			if (refsOfEndpoints.get(0).getEReferenceType() == prob2) {
				toProb2 = refsOfEndpoints.get(0);
				fromProb2 = toProb2.getEOpposite();
				tc = toProb2.getEContainingClass();
			} else {
				fromProb2 = refsOfEndpoints.get(0);
				toProb2 = fromProb2.getEOpposite();
				tc = fromProb2.getEReferenceType();
			}

			// Look for other EReferences between targetEdge and end1
			EReference refFromProb2ToTc = null;
			for (EReference testRef : prob2.getEReferences()) {
				if ((testRef.getEReferenceType() == tc)
						&& (testRef != fromProb2)) {
					refFromProb2ToTc = testRef;
				}
			}

			EReference refFromTcToProb2 = null;
			for (EReference testRef : tc.getEReferences()) {
				if ((testRef.getEReferenceType() == prob2)
						&& (testRef != toProb2)) {
					refFromTcToProb2 = testRef;
				}
			}

			// Only one EReference additional on this level
			if ((refFromProb2ToTc == null) && (refFromTcToProb2 == null)) {
				// Try to figure out, if that Erefs are the same as the found
				// ones
				if ((toProb2 == null) && (toEnd1 == null)) {
					// If the already found one and the new found one
					// point to the same direction, search further
					pointingRefs.remove(fromProb2);
					if (ereferenceWithOverwritten.get(fromProb2) != null) {
						pointingRefs.removeAll(ereferenceWithOverwritten
								.get(fromProb2));
					}
					findSecondEndpoint(candidate, end1, toEnd1, fromEnd1,
							ownRefs, pointingRefs, result,
							ereferenceWithOverwritten);
				} else if ((fromProb2 == null) && (fromEnd1 == null)) {
					ownRefs.remove(toProb2);
					if (ereferenceWithOverwritten.get(toProb2) != null) {
						ownRefs.removeAll(ereferenceWithOverwritten
								.get(toProb2));
					}
					findSecondEndpoint(candidate, end1, toEnd1, fromEnd1,
							ownRefs, pointingRefs, result,
							ereferenceWithOverwritten);
				} else if (((toProb2 != null) && (toEnd1 != null) && toProb2
						.getName().equals(toEnd1.getName()))
						|| ((fromProb2 != null) && (fromEnd1 != null) && fromProb2
								.getName().equals(fromEnd1.getName()))) {
					// Names are equal
					ownRefs.remove(toProb2);
					ownRefs.remove(fromProb2);
					if (ereferenceWithOverwritten.get(toProb2) != null) {
						ownRefs.removeAll(ereferenceWithOverwritten
								.get(toProb2));
					}
					if (ereferenceWithOverwritten.get(fromProb2) != null) {
						pointingRefs.removeAll(ereferenceWithOverwritten
								.get(fromProb2));
					}
					findSecondEndpoint(candidate, end1, toEnd1, fromEnd1,
							ownRefs, pointingRefs, result,
							ereferenceWithOverwritten);
				}
				// Default case, it's ok to take prob2 as endpoint 2
				else {
					toEnd2 = toProb2;
					fromEnd2 = fromProb2;
					if (!(((fromEnd1 == null) && (toEnd2 == null)) || ((fromEnd2 == null) && (toEnd1 == null)))) {
						System.out
								.println("Warning: EdgeClass "
										+ Ecore2TgAnalyzer
												.getQualifiedEClassName(candidate)
										+ " has not clear EReferences, the Transformation can be wrong.");
					}
				}
			}// end one direction

			else if (refFromProb2ToTc != null) {
				// Both directions are there - try to figure out, which
				// is the overwritten one
				if (((fromEnd1 == null) && (fromProb2 == null))
						|| ((toEnd1 == null) && (toProb2 == null))) {
					// the prob connection has the same direction like the end1
					fromEnd2 = refFromProb2ToTc;
					toEnd2 = fromEnd2.getEOpposite();
				} else if (((fromEnd1 != null) && (fromProb2 != null) && fromEnd1
						.getName().equals(fromProb2.getName()))
						|| ((toEnd1 != null) && (toProb2 != null) && toEnd1
								.getName().equals(toProb2.getName()))) {
					// Names are equal, take the other direction
					fromEnd2 = refFromProb2ToTc;
					toEnd2 = fromEnd2.getEOpposite();
				}

				// Default
				else {
					toEnd2 = toProb2;
					fromEnd2 = fromProb2;
					if (!((ereferenceWithOverwritten.get(fromEnd1) != null) && ereferenceWithOverwritten
							.get(fromEnd1).contains(refFromProb2ToTc))) {
						System.out
								.println("Warning: EdgeClass "
										+ Ecore2TgAnalyzer
												.getQualifiedEClassName(candidate)
										+ " has not clear EReferences, the Transformation can be wrong.");
					}
				}
			} else if (refFromTcToProb2 != null) {
				// Both directions are there - try to figure out, which
				// is the overwritten one
				if (((fromEnd1 == null) && (fromProb2 == null))
						|| ((toEnd1 == null) && (toProb2 == null))) {
					toEnd2 = refFromTcToProb2;
					fromEnd2 = toEnd2.getEOpposite();
				} else if (((fromEnd1 != null) && (fromProb2 != null) && fromEnd1
						.getName().equals(fromProb2.getName()))
						|| ((toEnd1 != null) && (toProb2 != null) && toEnd1
								.getName().equals(toProb2.getName()))) {
					// Names are equal, take the other direction
					toEnd2 = refFromTcToProb2;
					fromEnd2 = toEnd2.getEOpposite();
				}
				// Default
				else {
					toEnd2 = toProb2;
					fromEnd2 = fromProb2;
					if (!((ereferenceWithOverwritten.get(toEnd1) != null) && ereferenceWithOverwritten
							.get(toEnd1).contains(refFromTcToProb2))) {
						System.out
								.println("Warning: EdgeClass "
										+ Ecore2TgAnalyzer
												.getQualifiedEClassName(candidate)
										+ " has not clear EReferences, the Transformation can be wrong.");
					}
				}
			}
		} else {
			System.out.println("Warum ist das son mist???");
		}
		result.add(toEnd2);
		result.add(fromEnd2);
	}

	// -----------------------------------------------------------
	// -----------------------------------------------------------
	// -- STATIC analysis ----------------------------------------
	// -----------------------------------------------------------
	// -----------------------------------------------------------

	/**
	 * Returns the qualified name of an EReference in the Form
	 * ....Package1.EClassName.EReferenceName
	 * 
	 * @param ref
	 *            the EReference to find the qualified name for
	 * @return the qualified name of the given EReference
	 * */
	public static String getQualifiedReferenceName(EReference ref) {
		String name = "";
		EPackage epack = ref.getEContainingClass().getEPackage();
		EClass eclass = ref.getEContainingClass();
		EPackage temp = epack;
		name = epack.getName() + "." + eclass.getName() + "." + ref.getName();
		while (temp.getESuperPackage() != null) {
			temp = epack.getESuperPackage();
			name = temp.getName() + "." + name;
		}

		return name;
	}

	/**
	 * Returns the qualified name of an EClass in the Form
	 * ....Package1.EClassName
	 * 
	 * @param ec
	 *            the EClass to find the qualified name for
	 * @return the qualified name of the given EClass
	 * */
	public static String getQualifiedEClassName(EClass ec) {
		String name = "";
		EPackage epack = ec.getEPackage();
		EPackage temp = epack;
		name = epack.getName() + "." + ec.getName();
		while (temp.getESuperPackage() != null) {
			temp = epack.getESuperPackage();
			name = temp.getName() + "." + name;
		}

		return name;
	}

	/**
	 * Search in a Resource for an EClass with the given qualified name
	 * 
	 * @param qualname
	 *            Qualified name of the EClass to search for
	 * @return the EClass with the given qualified name or null if there is no
	 *         one
	 * */
	public static EClass getEClassByName(String qualname,
			Resource metamodelResource) {
		for (EObject ob : metamodelResource.getContents()) {
			EPackage p = (EPackage) ob;
			EClass ec = getEClassByName(qualname, p, p.getName());
			if (ec != null) {
				return ec;
			}
		}
		return null;
	}

	/**
	 * Search in an EPackage for an EClass with the given qualified name
	 * 
	 * @param qualname
	 *            Qualified name of the EClass to search for
	 * @param pack
	 *            EPackage to search in
	 * @param packqualname
	 *            Qualified name of the EPackage to determine the qualified name
	 *            of an EClass
	 * @return the EClass with the given qualified name or null if there is no
	 *         one
	 * */
	private static EClass getEClassByName(String qualname, EPackage pack,
			String packqualname) {
		// Look for EClassifiers
		for (EClassifier candidate : pack.getEClassifiers()) {
			if ((candidate instanceof EClass)
					&& qualname.equalsIgnoreCase(packqualname + "."
							+ candidate.getName())) {
				return (EClass) candidate;
			}
		}
		// Search in subpackages
		for (EPackage child : pack.getESubpackages()) {
			EClass temp = getEClassByName(qualname, child, packqualname + "."
					+ child.getName());
			if (temp != null) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * Searchs for the EReference with the qualified name
	 * 
	 * @param qualName
	 *            qualified name of the EReference
	 * @return the EReference with the qualified name or null if it does not
	 *         exist
	 * */
	public static EReference getEReferenceByName(String qualName,
			Resource metamodelResource) {
		int pointbeforerefname = qualName.lastIndexOf(".");
		String refname = qualName.substring(pointbeforerefname + 1);
		String classname = qualName.substring(0, pointbeforerefname);
		EClass ec = getEClassByName(classname, metamodelResource);
		if (ec == null) {
			System.err.println("EClass " + classname + " does not exist.");
			return null;
		}
		EReference keyref = null;
		for (EReference eref : ec.getEReferences()) {
			if (refname.equalsIgnoreCase(eref.getName())) {
				keyref = eref;
				break;
			}
		}
		return keyref;
	}

	/**
	 * Searches in the Resource for all EReferences that references the given
	 * EClass
	 * 
	 * @param e
	 *            The EClass, the pointing EReferences are searched for
	 * @param erefs
	 *            The ArrayList of all found EReferences
	 * */
	public static void getEReferences_that_point_on_EClass(
			Resource metamodelResource, EClass e, ArrayList<EReference> erefs) {
		for (EObject ob : metamodelResource.getContents()) {
			getEReferences_that_point_on_EClass(e, (EPackage) ob, erefs);
		}
	}

	/**
	 * Searches in the EPackage pack for all EReferences that references the
	 * given EClass
	 * 
	 * @param e
	 *            the EClass, the pointing EReferences are searched for
	 * @param pack
	 *            the package, where the pointing EReferences are searched
	 * @param erefs
	 *            the ArrayList of all found EReferences
	 * */
	private static void getEReferences_that_point_on_EClass(EClass e,
			EPackage pack, ArrayList<EReference> erefs) {
		// Iterate over all Classifiers
		for (EClassifier cl : pack.getEClassifiers()) {
			if (cl instanceof EClass) {
				EClass eclass = (EClass) cl;
				// Iterate over all EReferences
				for (EReference eref : eclass.getEReferences()) {
					if (eref.getEReferenceType() == e) {
						erefs.add(eref);
					}
				}
			}
		}
		// Iterate over all Subpackages
		for (EPackage childpack : pack.getESubpackages()) {
			getEReferences_that_point_on_EClass(e, childpack, erefs);
		}
	}

	/**
	 * Finds all subtypes of the EClasses in parents. The problem is, that there
	 * is no method to get all subtypes for an EClass - so we have to iterate
	 * over all EClasses again
	 * 
	 * @param metamodelResource
	 *            Resource of the metamodel to search in
	 * @param parents
	 *            list of declared EClasses to find children for
	 * 
	 * @return list with all subclasses of the given EClasses
	 * */
	public static ArrayList<EClass> getSubclassesOfEClasses(
			Resource metamodelResource, ArrayList<EClass> parents) {
		ArrayList<EClass> childs = new ArrayList<EClass>();
		for (EObject ob : metamodelResource.getContents()) {
			findSubclassesOfEClasses((EPackage) ob, parents, childs);
		}
		return childs;
	}

	/**
	 * Adds all Subtypes of the EClasses in parents into the childs list The
	 * Problem is, that there is no method to get all subtypes for an EClass -
	 * so we have to iterate over all EClasses again
	 * 
	 * @param pack
	 *            EPackage to search in
	 * @param parents
	 *            List of declared EClasses to find children for
	 * @param childs
	 *            List where the results are put in
	 * */
	private static void findSubclassesOfEClasses(EPackage pack,
			ArrayList<EClass> parents, ArrayList<EClass> childs) {
		for (EClassifier classi : pack.getEClassifiers()) {
			// if it is an EClass and
			if (classi instanceof EClass) {
				EClass testclass = (EClass) classi;
				// test for each edgecandidate, if it is a Supertype of the
				// eclass
				for (EClass edgecandidate : parents) {
					if (testclass.getEAllSuperTypes().contains(edgecandidate)) {
						childs.add(testclass);
						break;
					}
				}
			}
		}
		// Search in subpackages
		for (EPackage childpack : pack.getESubpackages()) {
			findSubclassesOfEClasses(childpack, parents, childs);
		}
	}

	/**
	 * Sorts the EClasses in the given list in place, so that afterward no
	 * EClass appears more than once and that one EClass has a greater index
	 * than its supertypes.
	 * 
	 * @param eclasses
	 *            list of EClasses to sort topological
	 * */
	public static void sortEClasses(ArrayList<EClass> eclasses) {
		// Sort edgeclasses
		int i = 0;
		while (i < eclasses.size()) {
			boolean increment = true;
			EClass eclass = eclasses.get(i);
			for (int j = i + 1; j < eclasses.size(); j++) {
				EClass compareeclass = eclasses.get(j);
				if (eclass == compareeclass) {
					eclasses.remove(i);
					increment = false;
					break;
				}
				if (eclass.getESuperTypes().contains(compareeclass)) {
					eclasses.remove(i);
					eclasses.add(j, eclass);
					increment = false;
					break;
				}
			}
			if (increment) {
				i++;
			}
		}
	}

	private HashMap<EReference, HashSet<EReference>> ref2set;

	public HashMap<EReference, HashSet<EReference>> getEReferenceToOverwriteCandidatesMap() {
		if (this.ref2set != null) {
			return this.ref2set;
		}
		if (this.edgeclasses == null) {
			this.searchForEdgeClasses(TransformParams.AUTOMATIC_TRANSFORMATION);
		}
		this.ref2set = new HashMap<EReference, HashSet<EReference>>();

		TreeSet<EClass> hyrachyCandidates = new TreeSet<EClass>();

		for (EClass ec : this.edgeclasses) {
			if (!ec.getESuperTypes().isEmpty()) {
				if (ec.getEReferences().size() == 1) {
					hyrachyCandidates.add(ec);
				}
			}
		}

		for (EClass c : hyrachyCandidates) {
			EReference keyref = c.getEReferences().get(0);
			HashSet<EReference> coll = new HashSet<EReference>();
			if (this.collectForKeyRef(c, keyref, coll)) {
				this.ref2set.put(keyref, coll);
			}
		}

		return this.ref2set;
	}

	private boolean collectForKeyRef(EClass c, EReference keyref,
			HashSet<EReference> coll) {
		boolean isambig = false;
		// Iterate over superclasses
		for (EClass sup : c.getESuperTypes()) {
			// If there is more than one EReferences that is the top
			if (sup.getEReferences().size() > 1) {
				// Determine whether the EReferences end at different classes
				HashSet<EClass> ends = new HashSet<EClass>();
				for (EReference ref : sup.getEReferences()) {
					ends.add(ref.getEReferenceType());
				}

				if (ends.size() > 1) {
					// if the single EReference is in the same hyrachy than both
					// EReferences of the supertype, one must be chosen, nothing
					// is clear
					if (keyref
							.getEReferenceType()
							.getEAllSuperTypes()
							.contains(
									this.ereferencesOfEdgeClasses.get(sup).get(
											0))
							&& keyref
									.getEReferenceType()
									.getEAllSuperTypes()
									.contains(
											this.ereferencesOfEdgeClasses.get(
													sup).get(1))) {
						coll.add(this.ereferencesOfEdgeClasses.get(sup).get(0));
						coll.add(this.ereferencesOfEdgeClasses.get(sup).get(1));
					}
					// if the single EReference is not in a hyrachy with both,
					// this EReference has a clear super, so everything is fine
					else {
						isambig = false;
					}
				} else {
					coll.add(this.ereferencesOfEdgeClasses.get(sup).get(0));
					coll.add(this.ereferencesOfEdgeClasses.get(sup).get(1));
				}
				continue;
			}
			// if there is only one EReference we have to look further above
			else {
				// add 0 or 1
				coll.addAll(sup.getEReferences());
				// recursive call
				isambig |= this.collectForKeyRef(sup, keyref, coll);
			}

		}
		return isambig;
	}
}