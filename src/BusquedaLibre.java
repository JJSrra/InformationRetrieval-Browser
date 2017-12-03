import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import java.util.ArrayList;


public class BusquedaLibre {

    EscenaResultadosTexto escenaResultados;
    ArrayList<QueryParser> consulta;
    Query query;
    TopDocs documentos;

    public BusquedaLibre(){

        escenaResultados = new EscenaResultadosTexto();

        consulta = new ArrayList<>();

        consulta.add(new QueryParser("title", new EnglishAnalyzer()));

        consulta.add(new QueryParser("abstract", new EnglishAnalyzer()));

        consulta.add(new QueryParser("source", new EnglishAnalyzer()));

    }

    public void busquedaLibre(ChoiceBox<String> campos, TextField contenido, IndexSearcher searcher, Stage window
                                ) throws Exception{

        if (campos.getValue() == "All Fields"){

            Query q1 = consulta.get(0).parse("title: "+contenido.getText());

            BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.SHOULD);

            Query q2 = consulta.get(1).parse("abstract: "+contenido.getText());

            BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.SHOULD);

            Query q3 = consulta.get(2).parse("source: "+contenido.getText());

            BooleanClause bc3 = new BooleanClause(q3,BooleanClause.Occur.SHOULD);

            Query q4 = new TermQuery(new Term("keywords index",contenido.getText()));

            BooleanClause bc4 = new BooleanClause(q4,BooleanClause.Occur.SHOULD);

            Query q5 = new TermQuery(new Term("keywords author",contenido.getText()));

            BooleanClause bc5 = new BooleanClause(q5,BooleanClause.Occur.SHOULD);

            Query q6 = new TermQuery(new Term("author",contenido.getText()));

            BooleanClause bc6 = new BooleanClause(q6,BooleanClause.Occur.SHOULD);

            BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();

            bqbuilder.add(bc1);
            bqbuilder.add(bc2);
            bqbuilder.add(bc3);
            bqbuilder.add(bc4);
            bqbuilder.add(bc5);
            bqbuilder.add(bc6);

            BooleanQuery bq = bqbuilder.build();

            documentos = searcher.search(bq, 2000);

            //System.out.println("Se han encontrado "+documentos.totalHits+" documentos.");

        }
        else if (campos.getValue() == "Keywords"){

            Query q4 = new TermQuery(new Term("keywords index",contenido.getText()));

            BooleanClause bcKewywordsIndex = new BooleanClause(q4,BooleanClause.Occur.SHOULD);

            Query q5 = new TermQuery(new Term("keywords author",contenido.getText()));

            BooleanClause bcKewywordsAuthor = new BooleanClause(q5,BooleanClause.Occur.SHOULD);

            BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();

            bqbuilder.add(bcKewywordsIndex);
            bqbuilder.add(bcKewywordsAuthor);

            BooleanQuery bq = bqbuilder.build();

            documentos = searcher.search(bq, 2000);

            //System.out.println("Se han encontrado  "+documentos.totalHits+" documentos para los campo Keywords.");

        }
        else{

            String campo = campos.getValue().toLowerCase();

            if (campos.getValue() == "Title")
                query = consulta.get(0).parse(campo+":"+contenido.getText());
            else if (campos.getValue() == "Abstract")
                query = consulta.get(1).parse(campo+":"+contenido.getText());
            else if (campos.getValue() == "Source")
                query = consulta.get(2).parse(campo+":"+contenido.getText());
            else
                query = new TermQuery(new Term("author",contenido.getText()));

            documentos = searcher.search(query, 2000);

            /*System.out.println("Se han encontrado  "+documentos.totalHits+" documentos para el campo "+campo+
            " con la búsqueda "+contenido.getText());*/

        }

        ObservableList<Documento> listaResultados = FXCollections.observableArrayList();

        for (ScoreDoc sd : documentos.scoreDocs){

            Document d = searcher.doc(sd.doc);

            listaResultados.add(new Documento(d.get("author"), d.get("title"), d.get("abstract"), d.get("source"),
                    d.get("link"), d.get("keywords author"), d.get("keywords index"), Integer.parseInt(d.get("year")),
                    Integer.parseInt(d.get("cited by"))));
        }

        escenaResultados.crearTablaDatos(window, listaResultados);

    }
}
