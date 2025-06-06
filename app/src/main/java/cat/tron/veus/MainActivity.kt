package cat.tron.veus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
//import android.view.View
//import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

   object objTTS {
      private var tts: TextToSpeech? = null
      fun set(t: TextToSpeech?) { tts = t }
      fun get(): TextToSpeech? { return tts }
      fun inici() { tts?.language = Locale("ca_ES") }
      operator fun invoke() = tts
   }

   object objVeus {
      private var idioma: String = "ca"
      private val iVeus: Map<String, Array<Voice>> = mapOf(
         "ca" to arrayOf(
            Voice("ca-es-x-caf-local", Locale("ca_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null)
         ),
         "es" to arrayOf(
            Voice("es-ES-language", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eea-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eec-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eed-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eee-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eef-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eea-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-es-x-eec-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-es-x-eed-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-US-language", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esd-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esf-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esc-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-sfb-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esc-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-esf-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-esd-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-sfb-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null)
         )
      )
      fun setIdioma(i: String) {idioma = i}
      fun getIdioma() = idioma
      fun getVeu(idioma: String, elem:Int): Voice = iVeus[idioma]!![elem]
      fun getVeus(idioma: String): Array<Voice>? { return iVeus[idioma] }
   }

   private var tts: TextToSpeech? = null
   private var idioma: Locale = Locale("ca", "ES")
   private val engine = "com.google.android.tts" // Motor de Google TTS
   private lateinit var notes: TextView
   private lateinit var notes_select: TextView
   private lateinit var selectVeu: Spinner
   private var opcionsVeu = arrayOf("veu_0")
   private lateinit var play: ImageButton
   private lateinit var selectVelocitat: Spinner
   private lateinit var selectRegistre: Spinner
   private val opcVelocitat = Array<String>(5) { n -> (0.9f + (n+1).toFloat()/10).toString() }
   //private val opcRegistre = Array<String>(20) { n -> ((n+1).toFloat()/10 + 0.2f).toString().substring(0,3) }
   private val opcRegistre = (3..20).map { (it * 0.1).toString() }.toTypedArray()
   private lateinit var selectIdioma: Spinner
   private val opcionsIdioma = arrayOf("Català", "English", "Español")
   private lateinit var activaIdioma: Button
   //private var idiomaItemSelected = false
   private var contador = 0


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      objTTS.set(TextToSpeech(this, this, engine))
      objTTS.inici()
      tts = objTTS.get()
      selectVeu = findViewById(R.id.selectVeu)
      play = findViewById(R.id.play)
      selectVelocitat = findViewById(R.id.selectVelocitat)
      selectRegistre = findViewById(R.id.selectRegistre)
      selectIdioma = findViewById(R.id.selectIdioma)
      notes = findViewById(R.id.notes)
      notes_select = findViewById(R.id.notes_select)

      generaFormulari(applicationContext)

      play.setOnClickListener {
         val veu = selectVeu.selectedItem.toString()
         val registre = if (selectRegistre.selectedItem.toString().isNotEmpty()) selectRegistre.selectedItem.toString().toFloat() else 1.0f
         val velocitat = if (selectVelocitat.selectedItem.toString().isNotEmpty()) selectVelocitat.selectedItem.toString().toFloat() else 1.0f
         //val registre: Float = if (registre.text.toString() != "") registre.text.toString().toFloat() else 1.0f
         //val velocitat: Float = if (velocitat.text.toString() != "") velocitat.text.toString().toFloat() else 1.0f
         val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
         opcionsVeu = Array(objVeus.getVeus(llengua)!!.size) { i -> "veu_${i}" }
         generaFormulari(applicationContext)
         canta(veu, registre, velocitat, llengua)
      }

      /*activaIdioma.setOnClickListener {
         val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
         notes_select.text = "selectIdioma: ${selectIdioma.selectedItem}\nllengua: $llengua"
         objVeus.setIdioma(llengua)
         tts?.language = Locale(llengua)
         canviaIdioma(llengua, applicationContext)
         opcionsVeu = Array(objVeus.getVeus(llengua)!!.size) { i -> "veu_${i}" }
         generaFormulari(applicationContext)
      }*/

      selectVeu.onItemSelectedListener.apply {
         notes_select.text = "selectVeu = ${selectVeu.selectedItem}"
      }
      selectVelocitat.onItemSelectedListener.apply {
         notes_select.text = "selectVelocitat = ${selectVelocitat.selectedItem}"
      }

      selectIdioma.onItemSelectedListener.apply {
         if (selectIdioma.selectedItem.toString().isNotEmpty()) {
            val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
            notes_select.text = "selectIdioma: ${selectIdioma.selectedItem}\nllengua: $llengua"
            objVeus.setIdioma(llengua)
            tts?.language = Locale(llengua)
            canviaIdioma(llengua, applicationContext)
            opcionsVeu = Array(objVeus.getVeus(llengua)!!.size) { i -> "veu_${i}" }
            generaFormulari(applicationContext)
         }
      }

      /*selectIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
         override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (idiomaItemSelected) {
               idiomaItemSelected = false
               contador += 1
               val llengua = parent.getItemAtPosition(position).toString().substring(0, 2).lowercase()
               //val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
               notes_select.text = "contador: ${contador}\nselectIdioma: ${selectIdioma.selectedItem.toString()}\nllengua: ${llengua}"
               objVeus.setIdioma(llengua)
               tts?.language = Locale(llengua)
               canviaIdioma(llengua, applicationContext)
               opcionsVeu = Array(objVeus.getVeus(llengua)!!.size) { i -> "veu_${i}" }
               generaFormulari(applicationContext)
            }else {
               idiomaItemSelected = true
            }
         }
         override fun onNothingSelected(parent: AdapterView<*>) {}
      }*/

   }

   // Inicia els elements del formulari
   private fun generaFormulari(context: Context) {
      selectVeu.adapter = ArrayAdapter(context, R.layout.spinner, opcionsVeu)
      selectVelocitat.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcVelocitat)
      selectRegistre.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcRegistre)
      selectIdioma.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcionsIdioma)
   }

   private fun canta(veuSeleccionada: String, registre: Float, velocitat: Float, llengua: String) {
      val text = mapOf(
         "ca" to "La senyora me les va regalar fa un any, sap? Estava molt contenta perquè havia fet bingo i deia que jo li havia donat sort aquell dia. Resulta que vaig deixar l'aspiradora mal aparcada, i ella va ensopegar-hi abans de sortir de casa.",
         "en" to "The lady gave them to me a year ago, you know. She was really happy because she'd won bingo and said I'd brought her luck that day. It turns out I'd left the vacuum cleaner parked wrong, and she tripped before leaving the house.",
         "es" to "La señora me las regaló hace un año, ¿sabe? Estaba muy contenta porque había hecho bingo y decía que yo le había dado suerte ese día. Resulta que dejé la aspiradora mal aparcada, y ella se tropezó antes de salir de casa."
      )
      val regexVeu = """.*?_([0-9]+)""".toRegex()
      val iVeu = regexVeu.find(veuSeleccionada)!!.groupValues[1].toInt()
      val veu = objVeus.getVeu(llengua, iVeu)

      notes.text = "veuSeleccionada: ${veuSeleccionada}\nveu: ${veu.name}\nregistre: ${registre}\nvelocitat: ${velocitat}\nidioma: $llengua"

      tts?.setPitch(registre)
      tts?.setSpeechRate(velocitat)
      tts?.speak(text[llengua], TextToSpeech.QUEUE_FLUSH, null, null)
      tts?.voice = veu
      while (tts?.isSpeaking==true) { true }
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
   }

   //TextToSpeech.OnInitListener
   override fun onInit(status: Int) {
      if (status == TextToSpeech.SUCCESS) {
         tts?.setEngineByPackageName(engine)
         val result = tts?.setLanguage(idioma)
         if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            print("idioma_no_soportat")
            // L'usuari haurà d'instal·lar l'enginy Google TTS
            val installIntent = Intent().apply {
               action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            }
            startActivity(installIntent)         }
      } else {
         print("error_inici_TTS")
      }
   }

   fun canviaIdioma(llengua: String, context: Context) {
      val displayMetrics = context.resources.displayMetrics
      val configuracio = context.resources.configuration
      configuracio.setLocale(Locale(llengua))
      context.resources.updateConfiguration(configuracio, displayMetrics)
      configuracio.locale = Locale(llengua)
      context.resources.updateConfiguration(configuracio, displayMetrics)
   }

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
