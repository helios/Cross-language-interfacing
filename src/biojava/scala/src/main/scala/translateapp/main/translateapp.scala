/**
 * Called from ./DNAtranslate
 *
 */

import scala.collection.JavaConversions._
import java.io._
import org.biojava.nbio.core.sequence._
import org.biojava.nbio.core.sequence.io._
import org.biojava.nbio.core.sequence.transcription._

object TranslateApp {
  val version = "1.0"

  def main(args: Array[String]) {
    val arglist = args.toList
    if (args.length == 0) {
      println("DNAtranslate "+version)
      println("""

  Translate a FASTA nucleotide sequence file to amino acids (protein) using
  BioJava4 translation

  Usage:

    ./DNAtranslate [-v] [--skip-translate] infile [--times n]

  Examples:

    ./DNAtranslate ../../../test/data/test-dna.fa

      """)
      sys.exit(1)
    }

    type OptionMap = Map[scala.Symbol, Any]
    
    def strOption(s: String) = {
      val regex = """(\d+)""".r
      s match {
        case regex(times) => Map('times -> times)
        case filename => Map('infile -> filename)
      }
    }
    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def switch(s : String) = (s(0) == '-')
      list match {
        case Nil => map
        case "-v" :: tail =>
                               nextOption(map ++ Map('verbose -> true), tail)
        case "--skip-translate" :: tail =>
                               nextOption(map ++ Map('skipTranslate -> true), tail)
        case "--times" :: value :: tail =>
                               nextOption(map ++ Map('times -> value), tail)

        case string :: opt2 :: tail if switch(opt2) => 
                               nextOption(map ++ strOption(string), list.tail)
        case string :: Nil =>  nextOption(map ++ strOption(string), list.tail)
        case option :: tail => println("Unknown option "+option) 
                               sys.exit(1) 
      }
      // Map('type -> false)
    }
    val options = nextOption(Map(),arglist)

    options.get( 'outfile ) match { 
      case Some(fn) => println("TranslateApp "+version)
                       println(options)
      case _ => 
    }
    def getBool(name : scala.Symbol) : Boolean = 
      options.get( name ) match {
        case Some(_) => true
        case None    => false
      }
   
    def getInt(name : scala.Symbol, default : Int) : Int = 
      options.get( name ) match {
        case Some(v) => v.toString.toInt
        case None    => default
      }
   
    val times = getInt('times,1) 
    val verbose = getBool('verbose)
    val skipTranslate = getBool('skipTranslate)

    // --- read input file
    val infile = options.get( 'infile ) match { 
      case Some(v) => v.toString
      case None => throw new Exception
    }
    if (verbose) {
      println("Reading Fasta file", infile, " x ", times)

    }
    IUPACParser.getInstance().getTable(1);
    IUPACParser.getInstance().getTable("UNIVERSAL");
    val engine = TranscriptionEngine.getDefault()
    val f = FastaReaderHelper.readFastaDNASequence(new File(infile))
    
    f.foreach { res: (String, DNASequence) => 
      val (id,dna) = res
      println(List(">",id).mkString) 
      if (skipTranslate) {
        println(dna)
      }
      else {
        // val s = new CodonSequence(dna)
        // println(dna)
        // println(dna.mkString.toUpperCase)
        val rna = dna.getRNASequence(engine)
        println(rna.getProteinSequence(engine))
      }
    }
  } // main
} // object


