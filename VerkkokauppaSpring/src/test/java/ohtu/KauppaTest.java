package ohtu;

import ohtu.verkkokauppa.Kauppa;
import ohtu.verkkokauppa.Pankki;
import ohtu.verkkokauppa.Tuote;
import ohtu.verkkokauppa.Varasto;
import ohtu.verkkokauppa.Viitegeneraattori;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class KauppaTest {
	Pankki pankki;
	Varasto varasto;
	Viitegeneraattori viitegeneraattori;
	Kauppa kauppa;
	
	
	@Before
	public void setUp() {
		pankki = mock(Pankki.class);
		varasto = mock(Varasto.class);
		viitegeneraattori = mock(Viitegeneraattori.class);
		
		kauppa = new Kauppa(varasto, pankki, viitegeneraattori);
	}
	
	@Test
	public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		// määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
		when(varasto.saldo(1)).thenReturn(10); 
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1); // ostetaan tuotetta numero 1 eli maitoa
		kauppa.tilimaksu("pekka", "12345");

		// sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(5));
	}
	
	@Test
	public void useammanOstoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.saldo(2)).thenReturn(10);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
		when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kokis", 4));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		kauppa.lisaaKoriin(2);
		kauppa.tilimaksu("pekka", "12345");

		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(9));  
	}
	
	@Test
	public void kahdenSamanOstoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		kauppa.lisaaKoriin(1);
		kauppa.tilimaksu("pekka", "12345");

		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(10));  
	}
	
	@Test
	public void toinenTuotteistaLoppuJaPankinMetodiaTilisiirtoKutsutaan() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.saldo(2)).thenReturn(0);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
		when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kokis", 4));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		kauppa.lisaaKoriin(2);
		kauppa.tilimaksu("pekka", "12345");

		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(5));  
	}
	
	@Test
	public void aloitaAsiointiNollaaEdellisenOstoksen() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		
		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		
		kauppa.tilimaksu("pekka", "12345");

		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(5));
	}
	
	@Test
	public void aloitaAsiointiPyytääUudenViitteen() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		when(viitegeneraattori.uusi()).thenReturn(15);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		
		kauppa.tilimaksu("pekka", "12345");
		
		verify(viitegeneraattori, times(1)).uusi();
		
		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		
		kauppa.tilimaksu("pekka", "12345");

		verify(viitegeneraattori, times(2)).uusi();
	}
	
	@Test
	public void tavaroidenPoistaminenOstoskorista() {
		when(viitegeneraattori.uusi()).thenReturn(42);
		
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.saldo(1)).thenReturn(10);
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
		when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

		kauppa.aloitaAsiointi();
		kauppa.lisaaKoriin(1);
		kauppa.poistaKorista(1);
		
		verify(varasto).palautaVarastoon(eq(new Tuote(1, "maito", 5)));
		
		kauppa.lisaaKoriin(1);
		
		kauppa.tilimaksu("pekka", "12345");

		verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(5));
	}
}