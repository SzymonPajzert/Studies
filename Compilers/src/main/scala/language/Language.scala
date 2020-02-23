package language

import scala.collection.mutable

trait Language {
  type Code
}

object LanguageRegister {
  private[LanguageRegister] val languages: mutable.Set[Language] = mutable.Set()

  def register(language: Language): Unit = {
    synchronized {
      languages.add(language)
    }
  }

  def getLanguages: List[Language] = {
    synchronized {
      languages.toList
    }
  }
}
