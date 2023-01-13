package app

import scalatags.Text.all._


import scala.collection.mutable.Buffer

object Reseptikirja extends cask.MainRoutes{

  case class Resepti(nimi: String) {
    type Raakaaine = String
    val aineet = Buffer[Raakaaine]()

  }

  val reseptit = collection.mutable.Map[String, Resepti]()

  @cask.staticFiles("/styling")
  def staticFileroutes() = "styling"

  @cask.staticFiles("/pics")
  def staticFileroutes1() = "pics"

  def reseptiSivu(title: String = "resepti")(parts: ConcreteHtmlTag[String]*) = {
    html(
        head(scalatags.Text.tags2.title("Reseptikirja"),
          meta(charset:="UTF-8"),
          link(
            rel :="stylesheet",
            href := "/styling/style.css"
          )
        ),
      body(
        parts
      )
    )
  }


  @cask.get("/reseptikirja")
  def resepti() = {
    reseptiSivu("/resepteja")(
      h1("Reseptikirja"),
      div(textAlign:= "center" ,
        p("Tervetuloa luomaan omaa reseptikirjaasi!"),
        p("Voit lisätä resepetejä alla olevasta napista ja valmiit reseptit näkyvät tällä sivulla."),
        h2("Lisää reseptisi nimi tähän"),
        form(action := "/uusiresepti", method := "post")(
          input(name := "reseptinnimi"),
          input(`type` := "submit", width := "10%")
        ),
        h2("Reseptisi:"),
        dl (
          for {
             resepti <- reseptit.keys.toSeq
          } yield li(
           a(href := "resepti/" + resepti, resepti)
          )
        ),
        img(src := "styling/goodsoup.png"),
      )
    )
  }

  @cask.postForm("/uusiresepti")
  def uusiResepti(reseptinnimi: String) = {
    if(reseptinnimi == "")
      cask.Redirect("/reseptikirja")
    else {
    reseptit += reseptinnimi -> Resepti(reseptinnimi)
    cask.Redirect("resepti/" + reseptinnimi)
    }
  }

  var nimi: String = ""
  @cask.postForm("/upload")
  def uploadFile(image: cask.FormFile, reseptinnimi: String) = {
    nimi = image.fileName
    val path= image.filePath
    javax.imageio.ImageIO.read(path.toFile).getHeight
    java.nio.file.Files.copy(path, new java.io.File("/styling/").toPath)
    cask.Redirect("resepti/" + reseptinnimi)
  }

  @cask.get("resepti/:nimi")
  def aineet(nimi: String) = {
    reseptiSivu("/aineita")(
      h1(nimi),
      h3(s"Tähän voit makusi mukaan kirjoittaa $nimi -reseptisi aloittamalla vaikkapa raaka-aineista tai annoskoosta."),

      form(action:= "/uusiaine", method := "post")(
        input(name:= "aineennimi"),
        input(name:= "reseptinnimi", `type` := "hidden", value := nimi),
        input(`type`:= "submit", width := "5%")
      ),
      dl (
        for {
           aine <- reseptit(nimi).aineet.toSeq
        } yield li(aine)
      ),

     form(action := "/upload", method := "post", enctype := "multipart/form-data")(
        input(name := "image", `type` := "file"),
       input(name:= "reseptinnimi", `type` := "hidden", value := nimi),
        input(`type` := "submit", width := "10%")
      ),
      img(src := "styling/" + nimi),

     a(href := "/reseptikirja", "Palaa takaisin etusivulle katsomaan muita reseptejäsi.")
    )
  }



  @cask.postForm("/uusiaine")
  def uusiAine(aineennimi: String, reseptinnimi: String) = {
    if(aineennimi == "")
      cask.Redirect("resepti/" + reseptinnimi)
    else {
    reseptit(reseptinnimi).aineet += aineennimi
    cask.Redirect("resepti/" + reseptinnimi)
    }
  }




  initialize()
}
