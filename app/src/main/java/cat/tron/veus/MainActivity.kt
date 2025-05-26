package cat.tron.veus

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

   object objTTS {
      private var tts: TextToSpeech? = null
      fun set(t: TextToSpeech?) { tts = t }
      fun get(): TextToSpeech? { return tts }
      fun inici() { tts?.language = Locale("ca_ES") }
      operator fun invoke() = tts
   }

   private var tts: TextToSpeech? = null
   private val idioma: Locale = Locale("ca", "ES")
   private val engine = "com.google.android.tts" // Motor de Google TTS
   private lateinit var notes: TextView
   private lateinit var heroi: Button
   private lateinit var brivall: Button
   private lateinit var velocitatHeroi: TextInputEditText
   private lateinit var velocitatBrivall: TextInputEditText
   private lateinit var registreHeroi: TextInputEditText
   private lateinit var registreBrivall: TextInputEditText


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      objTTS.set(TextToSpeech(this, this, engine))

      objTTS.inici()
      tts = objTTS.get()
      notes = findViewById(R.id.notes)
      heroi = findViewById(R.id.heroi)
      brivall = findViewById(R.id.brivall)
      velocitatHeroi = findViewById(R.id.velocitat_heroi)
      velocitatBrivall = findViewById(R.id.velocitat_brivall)
      registreHeroi = findViewById(R.id.registre_heroi)
      registreBrivall = findViewById(R.id.registre_brivall)

      heroi.setOnClickListener {
         val velocitat: Float = if (velocitatHeroi.text.toString() != "") velocitatHeroi.text.toString().toFloat() else 1.0f
         val registre: Float = if (registreHeroi.text.toString() != "") registreHeroi.text.toString().toFloat() else 1.0f
         speakCharacter(heroi.text.toString(), registre, velocitat)
      }

      brivall.setOnClickListener {
         val velocitat: Float = if (velocitatBrivall.text.toString() != "") velocitatBrivall.text.toString().toFloat() else 1.0f
         val registre: Float = if (registreBrivall.text.toString() != "") registreBrivall.text.toString().toFloat() else 1.0f
         speakCharacter(brivall.text.toString(), registre, velocitat)
      }
   }

   private fun speakCharacter(actor: String, pitch: Float, rate: Float) {
      val text = "La senyora me les va regalar fa un any, sap? Estava molt contenta perquè havia fet bingo i deia que jo li havia donat sort aquell dia. Resulta que vaig deixar l'aspiradora mal aparcada, i ella va ensopegar-hi abans de sortir de casa."
      notes.text = "actor: $actor \nregistre: ${pitch} \nvelocitat: ${rate}"
      /*
      val voiceName = when(actor) {
         "heroi" -> "ca-es-x-caf-local"
         "brivall" -> "ca-ES-language"
         else -> "ca-es-x-caf-local"
      }
      val voice = tts?.voices?.find { it.name == voiceName }
      voice?.let { tts?.voice = it }
      */
      val veu_brivall = Voice("ca-es-x-caf-local", Locale("ca_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_VERY_LOW, false, null)
      val veu_heroi = Voice("ca-es-x-caf-local", Locale("ca_ES"), Voice.QUALITY_HIGH, Voice.LATENCY_VERY_HIGH, false, null)
      val veu = when(actor) {
         "heroi" -> veu_heroi
         "brivall" -> veu_brivall
         else -> veu_heroi
      }
      tts?.voice = veu
      tts?.setPitch(pitch)
      tts?.setSpeechRate(rate)
      tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, actor)
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

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
