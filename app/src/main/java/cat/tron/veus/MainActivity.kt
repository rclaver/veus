package cat.tron.veus

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

   object objTTS {
      private var tts: TextToSpeech? = null
      fun set(t: TextToSpeech?) { tts = t }
      fun get(): TextToSpeech? { return tts }
      fun inici() { tts?.language = Locale("ca_ES") }

      fun llistaNomsDeVeus(local: String): Array<String> {
         var llista: Array<String> = arrayOf()
         tts?.voices?.forEach {
            if (it.locale.toString() == local) {
               llista += it.name
            }
         }
         return llista
      }
      fun llistaDeVeus(local: String): Array<Voice> {
         var llista: Array<Voice> = arrayOf()
         tts?.voices?.forEach {
            if (it.locale.toString() == local) {
               llista += it
            }
         }
         return llista
      }
      operator fun invoke() = tts
   }

   object objVeus {
      //private var idioma: String = "ca"
      private val iVeus: Map<String, Array<Voice>> = mapOf(
         "ca" to arrayOf(
            Voice("ca-es-x-caf-local", Locale("ca_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null)
         ),
         "es" to arrayOf(
            Voice("es-ES-language", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eea-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eec-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eed-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-es-x-eef-local", Locale("es_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-US-language", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-sfb-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esd-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
            Voice("es-us-x-esf-local", Locale("es_US"), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null),
         )
      )
      //veu_0: dona greu 0: es-ES-language
      //veu_1: dona greu 1: es-es-x-eea-local
      //veu_2: dona greu 2: es-es-x-eec-local
      //veu_3: home greu 1: es-es-x-eed-local
      //veu_4: home greu 2: es-es-x-eef-local
      //veu_5: dona US greu 0: es-US-language
      //veu_6: dona US greu 1: es-us-x-sfb-local
      //veu_7: home US greu 0: es-us-x-esd-local
      //veu_8: home US greu 1: es-us-x-esf-local

      //fun setIdioma(i: String) {idioma = i}
      //fun getIdioma() = idioma
      fun getVeu(idioma: String, elem:Int): Voice = iVeus[idioma]!![elem]
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
   private val opcVelocitat = Array<String>(6) { n -> (0.9f + (n+1).toFloat()/10).toString() }
   private lateinit var selectRegistre: NumberPicker
   //private val opcRegistre = Array<String>(20) { n -> ((n+1).toFloat()/10 + 0.2f).toString().substring(0,3) }
   private val opcRegistre = (3..20).map { (it * 0.1).toString().substring(0,3) }.toTypedArray()
   private lateinit var selectIdioma: Spinner
   private val opcionsIdioma = arrayOf("Català", "English", "Español")
   private var idiomaItemSelected = false
   private var registreSelectedItem = ""


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

      selectRegistre.setOnValueChangedListener { _, _, newVal ->
         registreSelectedItem = opcRegistre[newVal]
      }

      play.setOnClickListener {
         val veu = selectVeu.selectedItem.toString()
         val velocitat = if (selectVelocitat.selectedItem.toString().isNotEmpty()) selectVelocitat.selectedItem.toString().toFloat() else 1.0f
         val registre = if (registreSelectedItem.isNotEmpty()) registreSelectedItem.toFloat() else 1.0f
         //val velocitat: Float = if (velocitat.text.toString() != "") velocitat.text.toString().toFloat() else 1.0f
         //val registre: Float = if (registre.text.toString() != "") registre.text.toString().toFloat() else 1.0f
         val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
         canta(veu, registre, velocitat, llengua)
      }

      selectIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
         override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (idiomaItemSelected) {
               idiomaItemSelected = false
               //val llengua = selectIdioma.selectedItem.toString().substring(0, 2).lowercase()
               val llengua = parent.getItemAtPosition(position).toString().substring(0, 2).lowercase()
               //objVeus.setIdioma(llengua)
               tts?.language = Locale(llengua)
               canviaIdioma(llengua, applicationContext)
               opcionsVeu = Array(objVeus.getVeus(llengua)!!.size) { i -> "veu_${i}" }
               ompleSelectorDeVeus(applicationContext)
            }else {
               idiomaItemSelected = true
            }
         }
         override fun onNothingSelected(parent: AdapterView<*>) {}
      }

   }

   // Inicia els elements del formulari
   private fun generaFormulari(context: Context) {
      ompleSelectorDeVeus(context)
      selectVelocitat.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcVelocitat)
      selectRegistre.minValue = 0
      selectRegistre.maxValue = opcRegistre.size - 1
      selectRegistre.displayedValues = opcRegistre
      selectRegistre.wrapSelectorWheel = false
      selectIdioma.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcionsIdioma)
   }
   private fun ompleSelectorDeVeus(context: Context) {
      selectVeu.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, opcionsVeu)
   }

   private fun canta(veuSeleccionada: String, registre: Float, velocitat: Float, llengua: String) {
      val textVeu = mapOf(
         "ca" to "La senyora me les va regalar fa un any, sap? Estava molt contenta perquè havia fet bingo i deia que jo li havia donat sort aquell dia.",  // Resulta que vaig deixar l'aspiradora mal aparcada, i ella va ensopegar-hi abans de sortir de casa.",
         "en" to "The lady gave them to me a year ago, you know. She was really happy because she'd won bingo and said I'd brought her luck that day. It turns out I'd left the vacuum cleaner parked wrong, and she tripped before leaving the house.",
         "es" to "La señora me las regaló hace un año, ¿sabe? Estaba muy contenta porque había hecho bingo y decía que yo le había dado suerte ese día. Resulta que dejé la aspiradora mal aparcada, y ella se tropezó antes de salir de casa."
      )
      val regexVeu = """.*?_([0-9]+)""".toRegex()
      val iVeu = regexVeu.find(veuSeleccionada)!!.groupValues[1].toInt()
      val veu = objVeus.getVeu(llengua, iVeu)
      //notes.text = "veuSeleccionada: ${veuSeleccionada}\nveu: ${veu.name}\nregistre: ${registre}\nvelocitat: ${velocitat}\nidioma: $llengua"

      val veus = objTTS.llistaNomsDeVeus("en_US")
      var text = ""
      veus.forEach {text = text + it + "\n"}

      val veus2 = objTTS.llistaDeVeus("en_US")
      veus2.forEach {text = text + it.toString() + "\n"}
      desaArxiuADownloads(text, applicationContext)
      notes.text = text

      tts?.setPitch(registre)
      tts?.setSpeechRate(velocitat)
      tts?.speak(textVeu[llengua], TextToSpeech.QUEUE_FLUSH, null, null)
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

   fun desaArxiuADownloads(dades: String, context: Context) {
      val arxiu = "dadesVeus.txt"
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         saveToPublicDownloads(arxiu, dades, context)
      } else {
         saveToDownloadsLegacy(arxiu, dades, context)
      }
   }

   @RequiresApi(Build.VERSION_CODES.Q)
   private fun saveToPublicDownloads(arxiu: String, dades: String, context: Context) {
      val resolver = context.contentResolver

      val contentValues = ContentValues().apply {
         put(MediaStore.MediaColumns.DISPLAY_NAME, arxiu)
         put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
         put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
      }

      try {
         val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
         uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
               outputStream.write(dades.toByteArray())
            }
         }
      } catch (e: Exception) {
         e.printStackTrace()
      }
   }
   private fun saveToDownloadsLegacy(arxiu: String, dades: String, context: Context) {
      val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      if (!downloadsDir.exists()) {
         downloadsDir.mkdirs()
      }
      val file = File(downloadsDir, arxiu)
      FileOutputStream(file).use {
         it.write(dades.toByteArray())
      }
   }

   fun desaArxiuInternament(dades: String, context: Context) {
      val arxiu = "dadesVeus.txt"
      context.openFileOutput(arxiu, Context.MODE_PRIVATE).use {
         it.write(dades.toString().toByteArray())
      }
   }

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
