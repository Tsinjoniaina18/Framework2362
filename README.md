# Framework2362
Sprint web Dynamique

Ajouter dans web.xml:
    <init-param>
        <param-name>Controllers</param-name>
        <param-value>Controller</param-value>
    </init-param>

Importation des annotations:
    import mg.itu.prom16.annotation.Controller;
    import mg.itu.prom16.annotation.Get;

NB: La Jar du framework se trouve dans classes/jars qui est necessaire dans la librairie du projet Web

Le framework verifie et signale une erreur si:
    Le web.xml ne contient pas la balise <init-param>
    Il y a un doublon de lien lors de l'annotation d'une fonction en Get et liste les doublons
    La valeur de retour d'un fonction annoter Get n'est pas un String ou ModelView
    L'Url n'a pas de destination(aucune fonction n'est annoter a cet Url)

Ne pas oublier d'ajoute "-parameters" sur la commande de l'application lors de la compilation

Les fonction qui receptionne les donnees doivent retourner un ModelView

Les type valable sont:
    String
    Int
    Double

Lors de l'appel de la fonction sur la reception des donnees , les arguments peuvent etre annoter @Param ou non.
    si il est annote , le nom de l'input et du request.getParamater suivra la valeur de l'annotation
    sinon , le nom de l'input et du request.getParamater suivra le nom de l'argument lors du declaration de la fonction