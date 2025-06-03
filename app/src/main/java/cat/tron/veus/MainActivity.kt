package cat.tron.veus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.ArrayAdapter
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
            Voice("es-us-x-sfb-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-US-language", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esc-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-esd-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esf-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esc-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esf-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-sfb-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eef-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eec-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eed-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-ES-language", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eee-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eea-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-es-x-eea-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eec-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-es-x-eed-network", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null),
            Voice("es-us-x-esd-network", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, null)
         )
      )
      fun setIdioma(i: String) {idioma = i}
      fun get(idioma: String, elem:Int): Voice = iVeus[idioma]!![elem]
      fun getVeus(idioma: String): Array<Voice>? { return iVeus[idioma] }
   }

   private var tts: TextToSpeech? = null
   private val idioma: Locale = Locale("ca", "ES")
   private val engine = "com.google.android.tts" // Motor de Google TTS
   private lateinit var notes: TextView
   private lateinit var select_veu: Spinner
   private var opcionsVeu: Array<String> = arrayOf("veu_0")
   private lateinit var play: ImageButton
   private lateinit var velocitat: Spinner
   private lateinit var registre: Spinner
   private val opcVelocitat = Array(
      size=5,
      init = { n ->
         val i = 1.0f
         i+0.1f })
   private val opcRegistre = Array(
      size=20,
      init = { n ->
         val i = 0.3f
         i+0.1f })
   private lateinit var select_idioma: Spinner
   private val opcionsIdioma = arrayOf("Català", "English", "Español")


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      objTTS.set(TextToSpeech(this, this, engine))
      objTTS.inici()
      tts = objTTS.get()
      select_veu = findViewById(R.id.select_veu)
      play = findViewById(R.id.play)
      velocitat = findViewById(R.id.velocitat)
      registre = findViewById(R.id.registre)
      select_idioma = findViewById(R.id.select_idioma)
      notes = findViewById(R.id.notes)

      creaFormulari(applicationContext)

      play.setOnClickListener {
         val veu = select_veu.selectedItem.toString()
         val registre = registre.selectedItem.toString().toFloat()
         val velocitat = velocitat.selectedItem.toString().toFloat()
         //val registre: Float = if (registre.text.toString() != "") registre.text.toString().toFloat() else 1.0f
         //val velocitat: Float = if (velocitat.text.toString() != "") velocitat.text.toString().toFloat() else 1.0f
         val idioma = select_idioma.selectedItem.toString().substring(0, 2).lowercase()
         canta(veu, registre, velocitat, idioma)
      }

      select_veu.onItemSelectedListener.apply {
         opcionsVeu = Array(objVeus.getVeus(select_veu.selectedItem.toString())!!.size) {i -> "veu_${i}"}
         creaFormulari(applicationContext)
      }

      select_idioma.onItemSelectedListener.apply {
         objVeus.setIdioma(select_idioma.selectedItem.toString().substring(0, 2).lowercase())
         creaFormulari(applicationContext)
      }

   }

   // Crea els elements del formulari
   private fun creaFormulari(context: Context) {
      select_idioma.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcionsIdioma)

      select_veu = Spinner(context).apply {
         adapter = ArrayAdapter(context, R.layout.spinner, opcionsVeu)
         setPadding(10, 2, 0, 2)
      }
      velocitat = Spinner(context).apply {
         adapter = ArrayAdapter(context, R.layout.spinner, opcVelocitat)
         setPadding(10, 2, 0, 2)
      }
      registre = Spinner(context).apply {
         adapter = ArrayAdapter(context, R.layout.spinner, opcRegistre)
         setPadding(10, 2, 0, 2)
      }
   }

   private fun canta(veuSeleccionada: String, registre: Float, velocitat: Float, idioma: String) {
      val text = mapOf(
         "ca" to "La senyora me les va regalar fa un any, sap? Estava molt contenta perquè havia fet bingo i deia que jo li havia donat sort aquell dia. Resulta que vaig deixar l'aspiradora mal aparcada, i ella va ensopegar-hi abans de sortir de casa.",
         "en" to "The lady gave them to me a year ago, you know. She was really happy because she'd won bingo and said I'd brought her luck that day. It turns out I'd left the vacuum cleaner parked wrong, and she tripped before leaving the house.",
         "es" to "La señora me las regaló hace un año, ¿sabe? Estaba muy contenta porque había hecho bingo y decía que yo le había dado suerte ese día. Resulta que dejé la aspiradora mal aparcada, y ella se tropezó antes de salir de casa."
      )
      notes.text = "veuSeleccionada: ${veuSeleccionada}\nregistre: ${registre}\nvelocitat: ${velocitat}\nidioma: $idioma"

      val regexVeu = """.*?_([0-9]+)""".toRegex()
      val iVeu = regexVeu.find(veuSeleccionada)!!.groupValues[1].toInt()
      val veu = objVeus.getVeus(idioma)!![iVeu]

      tts?.setPitch(registre)
      tts?.setSpeechRate(velocitat)
      tts?.speak(text[idioma], TextToSpeech.QUEUE_FLUSH, null, null)
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

   fun canviaIdioma(idioma: String, context: Context) {
      val displayMetrics = context.resources.displayMetrics
      val configuracio = context.resources.configuration
      configuracio.setLocale(Locale(idioma))
      context.resources.updateConfiguration(configuracio, displayMetrics)
      configuracio.locale = Locale(idioma)
      context.resources.updateConfiguration(configuracio, displayMetrics)
   }

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
