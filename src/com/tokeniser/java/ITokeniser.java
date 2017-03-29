package com.tokeniser.java;

import java.io.IOException;
import java.util.List;

/// <summary>
   /// ·Ö´ÊÆ÷½Ó¿Ú
   /// </summary>
   public interface ITokeniser
   {
       List<String> partition(String input) throws IOException;
   }
