package PorPreZha.Model;


import javax.tools.Tool;

public class PrivateObjectiveCard extends ObjectiveCard {


    public enum PRC {

        PRC1("Sfumature Rosse - Privata", 11, "Somma dei valori su tutti i dadi rossi", 0),
        PRC2("Sfumature Gialle - Privata", 12, "Somma dei valori su tutti i dadi gialli", 0),
        PRC13("Sfumature Verdi - Privata", 13, "Somma dei valori su tutti i dadi verdi", 0),
        PRC14("Sfumature Blu - Privata", 14, "Somma dei valori su tutti i dadi blu", 0),
        PRC15("Sfumature Viola - Privata", 15, "Somma dei valori su tutti i dadi viola", 0);
        // Notare che i valori dei punteggi sono posti a 0 di default, in sede di calcolo punteggio si effettuerà la valutazione del punteggio attribuito alla data carta


        private final int ID;
        private final String description;
        private final int numScore;
        private final String name;

        PRC(String name, int ID, String description, int numScore) {

            this.name = name;
            this.ID = ID;
            this.description = description;
            this.numScore = numScore;
    }



    }

    public PrivateObjectiveCard(PRC prc){
        super(board);
        this.prc=prc;

    }

    public  PRC prc;


}
