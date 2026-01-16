#!/usr/bin/python3
# -*- coding: UTF8 -*-
"""
@created: 17-10-2025
@author: rafael
@description: Mostra de les veus del model Coqui tts

Instalació prèvia:
sudo apt-get install python-tk
sudo apt-get install python3-pil python3-pil.imagetk
pip install --no-cache-dir pydub torch elevenlabs
"""

import warnings
warnings.filterwarnings("ignore", message="pkg_resources is deprecated")

import tkinter as tk
from tkinter import ttk
import torch
from TTS.api import TTS
from pydub import AudioSegment
from pydub.playback import play

import elevenlabs as e
from elevenlabs.client import ElevenLabs


class MostraDeVeus:
   def __init__(self, root):
      self.root = root
      self.root.title("Veus")
      self.root.minsize(600, 400)

      # Variables
      self.twav = "tmp/tmp.wav"
      self.arxiu_sortida = "tmp/llista_de_veus.txt"
      self.selected_voice = tk.StringVar(value="")
      self.veu_actual = tk.StringVar(value="")
      self.veu_actual2 = tk.StringVar(value="")
      self.dir_images = "static/img"
      self.images = {}
      self.model = ['coqui-tts', 'ElevenLabs']
      self.model_actual = "coqui-tts"
      self.client = None
      self.tts = None
      self.n_voice = 0
      self.voices = {}
      self.nou_nom = tk.StringVar()
      self.genere = tk.StringVar()
      self.text = "Que tingui sentit de l’humor no significa que no sigui femenina. Estaràs d’acord amb mi que les dones, en teoria, poden tenir sentit de l’humor."
      self.missatge = tk.StringVar()
      self.bg_color = '#dddddd'

      self.carrega_imatges()
      self.carrega_veus()
      self.create_widgets()

   def carrega_imatges(self):
      self.images['anterior'] = tk.PhotoImage(file=f"{self.dir_images}/anterior.png")
      self.images['inici'] = tk.PhotoImage(file=f"{self.dir_images}/inici.png")
      self.images['seguent'] = tk.PhotoImage(file=f"{self.dir_images}/seguent.png")
      self.images['desar'] = tk.PhotoImage(file=f"{self.dir_images}/desar.png")
      self.images['sortir'] = tk.PhotoImage(file=f"{self.dir_images}/sortir.png")

   def carrega_veus(self):
      if self.model_actual == "coqui-tts":
         device = "cuda" if torch.cuda.is_available() else "cpu"
         self.tts = TTS("tts_models/ca/custom/vits", progress_bar=False).to(device)
         self.voices = self.tts.speakers

      elif self.model_actual == "ElevenLabs":
         try:
            with open("static/API_Key_ElevenLabs", 'r') as f:
               k = f.read()
            self.client = ElevenLabs(api_key = k)
            self.voices.clear()
            el_veus = self.client.voices.get_all()
            for v in el_veus.voices:
               if v.fine_tuning.language == 'ca' or v.fine_tuning.language == 'es':
                  self.voices.append(v.voice_id)
         except Exception as ex:
            self.missatge.set(f"Error carrega_veus ({self.model_actual}): {str(ex)}")

      try:
         self.n_voice = 0
         self.voice_combo.configure(values=self.voices)
      except:
         pass

   def create_widgets(self):
      # Frame principal
      main_frame = ttk.Frame(self.root, padding="10")
      main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))

      # Configurar grid weights
      self.root.columnconfigure(0, weight=1)
      self.root.rowconfigure(0, weight=1)
      main_frame.columnconfigure(1, weight=1)
      main_frame.rowconfigure(6, weight=1)

      # Títol
      ttk.Label(main_frame, text="Mostra de les veus del model Coqui tts", font=("Arial",16,"bold")).grid(row=0, column=0, columnspan=2, pady=(0, 10))

      # Selector de model
      ttk.Label(main_frame, text="model: ", font=("Arial",9,"bold")).grid(row=1, column=0, sticky=(tk.N,tk.E), pady=(5,10))
      model_frame = ttk.Frame(main_frame)
      model_frame.grid(row=1, column=1, sticky=(tk.N, tk.W), pady=(10,10))
      model_frame.columnconfigure(0, weight=1)

      # Combobox per seleccionar el model
      self.model_combo = ttk.Combobox(
         model_frame,
         values=self.model,
         state="readonly",
         font=("Arial",9),
         width=20
      )
      self.model_combo.grid(row=0, column=0, sticky=tk.W, padx=5)

      # Vincular l'event de canvi de selecció
      self.model_combo.bind('<<ComboboxSelected>>', self.on_model_change)

      # Selector de veus
      ttk.Label(main_frame, text="veu: ", font=("Arial",9,"bold")).grid(row=2, column=0, sticky=(tk.N,tk.E), pady=(5,10))
      voice_frame = ttk.Frame(main_frame)
      voice_frame.grid(row=2, column=1, sticky=(tk.N, tk.W), pady=(10,10))
      voice_frame.columnconfigure(0, weight=1)

      # Combobox per seleccionar veu
      self.voice_combo = ttk.Combobox(
         voice_frame,
         values=self.voices,
         state="readonly",
         font=("Arial",9),
         width=70
      )
      self.voice_combo.grid(row=0, column=0, sticky=tk.W, padx=5)

      # Vincular l'event de canvi de selecció
      self.voice_combo.bind('<<ComboboxSelected>>', self.on_voice_change)

      # Etiqueta que mostra el codi de la veu seleccionada
      #ttk.Label(main_frame, textvariable=self.veu_actual, font=("Arial",9)).grid(row=2, column=1, sticky=(tk.N,tk.W))
      #ttk.Label(main_frame, textvariable=self.veu_actual2, font=("Arial",9)).grid(row=3, column=1, sticky=(tk.N,tk.W))

      # Quadre d'entrada de dades
      ttk.Label(main_frame, text="nou nom: ", font=("Arial",9,"bold")).grid(row=3, column=0, sticky=(tk.N,tk.E), pady=(5,5))
      nou_nom_frame = ttk.Frame(main_frame)
      nou_nom_frame.grid(row=3, column=1, sticky=(tk.N,tk.W), pady=(5,5))
      ttk.Entry(main_frame, textvariable=self.nou_nom, font=("Arial",9)).grid(row=3, column=1, sticky=(tk.N,tk.W))

      # Àrea de selecció de gènere
      ttk.Label(main_frame, text="gènere: ", font=("Arial",9,"bold")).grid(row=4, column=0, sticky=(tk.N,tk.E), pady=(5,5))
      genere_frame = ttk.Frame(main_frame)
      genere_frame.grid(row=4, column=1, sticky=(tk.N,tk.W), pady=(5,5))
      tk.Radiobutton(genere_frame, text="home", variable=self.genere, value="home", font=("Arial",9), bg=self.bg_color).grid(row=0, column=0, sticky=tk.W, padx=5)
      tk.Radiobutton(genere_frame, text="dona", variable=self.genere, value="dona", font=("Arial",9), bg=self.bg_color).grid(row=0, column=1, sticky=tk.W, padx=5)

      # Botons de control
      button_frame = ttk.Frame(main_frame)
      button_frame.grid(row=5, column=0, columnspan=3, sticky=tk.N, pady=(10,0))

      ttk.Button(button_frame, image=self.images['anterior'], command=self.anterior).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['inici'], command=self.text_to_audio).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['seguent'], command=self.seguent).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['desar'], command=self.desar).pack(side=tk.LEFT, padx=(15,0))
      ttk.Button(button_frame, image=self.images['sortir'], command=self.root.destroy).pack(side=tk.LEFT, padx=(15,0))

      # Etiqueta que mostra un missatge
      ttk.Label(main_frame, textvariable=self.missatge, font=("Arial",9)).grid(row=6, column=0, columnspan=2, sticky=(tk.N,tk.W), pady=5)


   def text_to_audio(self):
      self.mostra_veu_actual()

      # Text to speech to a file
      self.tts.tts_to_file(self.text, speaker=self.voices[self.n_voice], file_path=self.twav, verbose=False)
      audio = AudioSegment.from_wav(self.twav)
      play(audio)

   def anterior(self):
      self.n_voice -= 1
      self.text_to_audio()

   def seguent(self):
      self.n_voice += 1
      self.text_to_audio()

   def desar(self):
      """Desa el nom de la veu actual en un arxiu de text"""
      registre = f"{self.voices[self.n_voice]}\t{self.nou_nom.get()}\t{self.genere.get()}\n"
      try:
         with open(self.arxiu_sortida, 'a', encoding='utf-8') as file:
            file.write(registre)
      except Exception as ex:
         self.missatge.set(f"Error en desar: {str(ex)}")

   def on_model_change(self, event):
      '''Actualitza la llista de veus quan canvia la selecció del model'''
      self.model_actual = self.model_combo.get()
      self.carrega_veus()
      #self.voice_combo.set(self.voices[0])

   def on_voice_change(self, event):
      '''Actualitza l'etiqueta de la veu quan canvia la selecció'''
      selected_voice_name = self.voice_combo.get()
      self.selected_voice.set(selected_voice_name)
      self.veu_actual.set(self.veu_retallada(selected_voice_name))

      self.n_voice = self.voices.index(selected_voice_name)
      self.text_to_audio()

   def mostra_veu_actual(self):
      '''Actualitza l'etiqueta de la veu'''
      self.veu_actual.set(self.veu_retallada(self.voices[self.n_voice]))
      self.missatge.set(self.veu_retallada(self.voices[self.n_voice]))
      self.selected_voice.set(self.voices[self.n_voice])
      self.voice_combo.set(self.voices[self.n_voice])

   def veu_retallada(self, text):
      self.veu_actual2.set(f"- {text[76:]}" if (text[76:] != "") else "")
      return text[0:76]

if __name__ == "__main__":
   root = tk.Tk()
   MostraDeVeus(root)
   root.mainloop()
