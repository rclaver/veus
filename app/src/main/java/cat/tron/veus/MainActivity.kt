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
import androidx.core.view.get
import androidx.core.view.isGone
import java.util.Locale
import kotlin.math.round

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
      fun getIdioma() = idioma
      fun get(idioma: String, elem:Int): Voice = iVeus[idioma]!![elem]
      fun getVeus(idioma: String): Array<Voice>? { return iVeus[idioma] }
   }

   private var tts: TextToSpeech? = null
   private var idioma: Locale = Locale("ca", "ES")
   private val engine = "com.google.android.tts" // Motor de Google TTS
   private lateinit var notes: TextView
   private lateinit var selectVeu: Spinner
   private var opcionsVeu = arrayOf("veu_0")
   private lateinit var play: ImageButton
   private lateinit var selectVelocitat: Spinner
   private lateinit var selectRegistre: Spinner
   private val opcVelocitat = Array<Float>(5) { n -> 0.9f + (n+1).toFloat()/10 }
   private val opcRegistre = Array<Float>(20) { n -> round((n+1).toFloat()/10 + 0.2f) }
   private lateinit var selectIdioma: Spinner
   private val opcionsIdioma = arrayOf("Català", "English", "Español")


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

      generaFormulari(applicationContext)

      play.setOnClickListener {
         val veu = selectVeu.selectedItem.toString()
         val registre = if (selectRegistre.get(0).isGone) selectRegistre.selectedItem.toString().toFloat() else 1.0f
         val velocitat = if (selectVelocitat.get(0).isGone) selectVelocitat.selectedItem.toString().toFloat() else 1.0f
         //val registre: Float = if (registre.text.toString() != "") registre.text.toString().toFloat() else 1.0f
         //val velocitat: Float = if (velocitat.text.toString() != "") velocitat.text.toString().toFloat() else 1.0f
         val idiom = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
         canta(veu, registre, velocitat, idiom)
      }

      selectVeu.onItemSelectedListener.apply {
         opcionsVeu = Array(objVeus.getVeus(objVeus.getIdioma())!!.size) { i -> "veu_${i}" }
         generaFormulari(applicationContext)
      }

      selectIdioma.onItemSelectedListener.apply {
         if (selectIdioma.selectedItem.toString().isNotEmpty()) {
            val idiom = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
            objVeus.setIdioma(idiom)
            canviaIdioma(idiom, applicationContext)
            generaFormulari(applicationContext)
         }
      }

   }

   // Inicia els elements del formulari
   private fun generaFormulari(context: Context) {
      selectIdioma.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcionsIdioma)
      selectVeu.adapter = ArrayAdapter(context, R.layout.spinner, opcionsVeu)
      selectVelocitat.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcVelocitat)
      selectRegistre.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcRegistre)
   }

   private fun canta(veuSeleccionada: String, registre: Float, velocitat: Float, idiom: String) {
      val text = mapOf(
         "ca" to "La senyora me les va regalar fa un any, sap? Estava molt contenta perquè havia fet bingo i deia que jo li havia donat sort aquell dia. Resulta que vaig deixar l'aspiradora mal aparcada, i ella va ensopegar-hi abans de sortir de casa.",
         "en" to "The lady gave them to me a year ago, you know. She was really happy because she'd won bingo and said I'd brought her luck that day. It turns out I'd left the vacuum cleaner parked wrong, and she tripped before leaving the house.",
         "es" to "La señora me las regaló hace un año, ¿sabe? Estaba muy contenta porque había hecho bingo y decía que yo le había dado suerte ese día. Resulta que dejé la aspiradora mal aparcada, y ella se tropezó antes de salir de casa."
      )
      notes.text = "veuSeleccionada: ${veuSeleccionada}\nregistre: ${registre}\nvelocitat: ${velocitat}\nidioma: $idiom"

      val regexVeu = """.*?_([0-9]+)""".toRegex()
      val iVeu = regexVeu.find(veuSeleccionada)!!.groupValues[1].toInt()
      val veu = objVeus.getVeus(idiom)!![iVeu]

      tts?.setPitch(registre)
      tts?.setSpeechRate(velocitat)
      tts?.speak(text[idiom], TextToSpeech.QUEUE_FLUSH, null, null)
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

   fun canviaIdioma(idiom: String, context: Context) {
      val displayMetrics = context.resources.displayMetrics
      val configuracio = context.resources.configuration
      configuracio.setLocale(Locale(idiom))
      context.resources.updateConfiguration(configuracio, displayMetrics)
      configuracio.locale = Locale(idiom)
      context.resources.updateConfiguration(configuracio, displayMetrics)
   }

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
