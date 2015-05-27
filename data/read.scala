import java.nio.file.{Files, Paths}

def fromByteArray(b: Array[Byte],i: Int): Int = {
	var a: Int = 0
	a = (b(i) << 24) + (b(i + 1) << 16) + (b(i + 2) << 8) + b(i + 3)
	a
}
val byteArray = Files.readAllBytes(Paths.get("train-images-idx3-ubyte")) 
var images = Seq[Array[Byte]]()
var a = 0
var b = 0
for(a <- 0 until 60000){
	var image = Array[Byte]();
	for(b <- 0 until 28){
		image = image :+ byteArray(a*28*28 + b + 16);
	}
	images = images :+ image;
}