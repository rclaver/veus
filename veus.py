#!/usr/bin/python3
# -*- coding: UTF8 -*-
"""
@created: 17-10-2025
@author: rafael
@description: Mostra de les veus del model Coqui tts

Instalació prèvia:
sudo apt-get install python-tk
sudo apt-get install python3-pil python3-pil.imagetk
pip3 install --user pydub speechrecognition
"""

import warnings
warnings.filterwarnings("ignore", message="pkg_resources is deprecated")

import tkinter as tk
from tkinter import ttk
import torch
from TTS.api import TTS
from pydub import AudioSegment
from pydub.playback import play
#import speech_recognition as sr
#import wave


class MostraDeVeus:
   def __init__(self, root):
      self.root = root
      self.root.title("Veus")
      self.root.minsize(600, 200)

      # Variables
      self.arxiu_wav = "tmp/tmp.wav"
      self.arxiu_sortida = "tmp/llista_de_veus.txt"
      self.selected_voice = tk.StringVar(value="")
      self.veu_actual = tk.StringVar(value="")
      self.veu_actual2 = tk.StringVar(value="")
      self.dir_images = "static/img"
      self.images = {}
      self.tts = None
      self.n_voice = 0
      self.voices = {}
      self.genere = tk.StringVar()
      self.text = "Que tingui sentit de l’humor no significa que no sigui femenina. Estaràs d’acord amb mi que les dones, en teoria, poden tenir sentit de l’humor."
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
      device = "cuda" if torch.cuda.is_available() else "cpu"
      self.tts = TTS("tts_models/ca/custom/vits", progress_bar=False).to(device)
      self.voices = self.tts.speakers

   def create_widgets(self):
      # Frame principal
      main_frame = ttk.Frame(self.root, padding="10")
      main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))

      # Configurar grid weights
      self.root.columnconfigure(0, weight=1)
      self.root.rowconfigure(0, weight=1)
      main_frame.columnconfigure(1, weight=1)
      main_frame.rowconfigure(4, weight=1)

      # Títol
      ttk.Label(main_frame, text="Mostra de les veus del model Coqui tts", font=("Arial",16,"bold")).grid(row=0, column=0, columnspan=3, pady=(0, 10))

      # Selector de veus
      ttk.Label(main_frame, text="veu:", font=("Arial",9,"bold")).grid(row=1, column=0, sticky=(tk.N,tk.W), pady=(5,10))
      voice_frame = ttk.Frame(main_frame)
      voice_frame.grid(row=1, column=1, columnspan=2, sticky=(tk.N, tk.W, tk.W), pady=(10,10))
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
      ttk.Label(main_frame, textvariable=self.veu_actual, font=("Arial",9)).grid(row=2, column=1, sticky=(tk.N,tk.W))
      ttk.Label(main_frame, textvariable=self.veu_actual2, font=("Arial",9)).grid(row=3, column=1, sticky=(tk.N,tk.W))

      # Àrea de selecció de gènere
      ttk.Label(main_frame, text="gènere:", font=("Arial",9,"bold")).grid(row=4, column=0, sticky=(tk.N,tk.W), pady=(10,0))
      genere_frame = ttk.Frame(main_frame)
      genere_frame.grid(row=4, column=1, sticky=(tk.N,tk.W), pady=0)
      tk.Radiobutton(genere_frame, text="home", variable=self.genere, value="home", font=("Arial",9), bg=self.bg_color).grid(row=0, column=0, sticky=tk.W)
      tk.Radiobutton(genere_frame, text="dona", variable=self.genere, value="dona", font=("Arial",9), bg=self.bg_color).grid(row=1, column=0, sticky=tk.W)

      # Botons de control
      button_frame = ttk.Frame(main_frame)
      button_frame.grid(row=5, column=0, columnspan=3, sticky=tk.N, pady=(10,0))

      ttk.Button(button_frame, image=self.images['anterior'], command=self.anterior).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['inici'], command=self.text_to_audio).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['seguent'], command=self.seguent).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['desar'], command=self.desar).pack(side=tk.LEFT, padx=(15,0))
      ttk.Button(button_frame, image=self.images['sortir'], command=self.root.destroy).pack(side=tk.LEFT, padx=(15,0))


   def text_to_audio(self):
      self.mostra_veu_actual()
      #print("tts: ", self.tts)
      #print("tts.speakers: ", self.tts.speakers)

      # Text to speech list of amplitude values as output
      #wav = self.tts.tts(self.text, speaker=self.voices[self.n_voice])
      #play(wav)

      # Text to speech to a file
      self.tts.tts_to_file(self.text, speaker=self.voices[self.n_voice], file_path=self.arxiu_wav, verbose=False)
      audio_pendent = AudioSegment.from_wav(self.arxiu_wav)
      play(audio_pendent)

   def anterior(self):
      self.n_voice -= 1
      self.text_to_audio()

   def seguent(self):
      self.n_voice += 1
      self.text_to_audio()

   def desar(self):
      """Desa el nom de la veu actual en un arxiu de text"""
      registre = f"{self.voices[self.n_voice]}\t{self.genere}"
      try:
         with open(self.arxiu_sortida, 'a', encoding='utf-8') as file:
            file.write(registre)
      except Exception as e:
         self.veu_actual.set(self.veu_retallada(f"Error en desar: {str(e)}"))

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
      self.selected_voice.set(self.voices[self.n_voice])
      self.voice_combo.set(self.voices[self.n_voice])

   def veu_retallada(self, text):
      if (text[70:] != ""):
         self.veu_actual2.set(f"- {text[70:]}")
      else:
         self.veu_actual2.set("")
      return text[0:70]


if __name__ == "__main__":
   root = tk.Tk()
   MostraDeVeus(root)
   root.mainloop()
