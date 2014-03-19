package fr.unistra.pelican.algorithms;

/**
 * This algorithm is an example which aims to demonstrate how to create a conform class.
 * 
 * @author Lefevre, Florent Sollier
 *
 */
public class Exemple extends Algorithm {

	// Définition des paramètres d'entrée (obligatoires ou optionnels) et de sortie
	// sans convention imposée de nommage mais avec une valeur par défaut pour les options,
	// une visibilité publique et une représentation sous forme d'objets (pas des types primitifs)

	// Exemples :
	/**
	 * Here the description of the attribute exempleInput1.
	 */
	public Image exempleInput1 ;
	
	/**
	 * Here the description of the attribute exempleInput2 .
	 */
	public Integer exempleInput2 ;
	
	/**
	 * Here the description of the attribute exempleOutput1.
	 */
	public Image exempleOutput1 ;
	
	/**
	 * Here the description of the attribute exempleOutput2.
	 */
	public Integer exempleOutput2 ;
	
	/**
	 * Here the description of the attribute exempleOption1.
	 */
	public Integer exempleOption1 = 1;
	
	/**
	 * Here the description of the attribute exempleOption2.
	 */
	public Integer exempleOption2 = 0;
	

	// Définition du constructeur par défaut
	public Exemple() {
		// définition de la description d'aide
		super.help="description en une ou quelques lignes de l'algorithme";
		// définition des listes ordonnées de paramètres d'entrée, de sortie, et d'option
		// telles qu'elles seront utilisées lors des appels à  process
		super.inputs="exempleInput1,exempleInput2";
		super.outputs="exempleOutput1,exempleOutput2";
		// et seulement s'il y a des options...
		// mettre les options les plus fréquemment utilisées en tête de liste
		super.options="exempleOption1,exempleOption2";
		}

	// Définition de la méthode de traitement (pas de changement)
	public void launch() {
		// ici on doit vérifier la validité du contenu des paramètres d'entrée et des options
		// car seule une vérification de type aura été faite dans la classe abstraite
		...
		// Ã  la fin de cette méthode, il faudra que tous les paramètres de sortie soient correctement définis
		exempleOutput1 = ImageApresTraitement1;
		exempleOutput2 = ImageApresTraitement1..getBDim();
		}

	/**
	 * Here the description of what does the exec method (which basically is the description of 
	 * what does the algorithm that you can copy/paste from the top).
	 * 
	 * @param exempleInput1 Here the description of the parameter exempleInput1.
	 * @param exempleInput2 Here the description of the parameter exempleInput2.
	 * @return Here the description of what returns the exec method.
	 */
	public static Image exec(Image exempleInput1, int exempleInput2) {
		return (Image) new Exemple().process(exempleInput1,exempleInput2);
		}

	}
