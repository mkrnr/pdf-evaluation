require 'anystyle/parser'
require 'fileutils'

inputDir = ARGV[0]
outputDir = ARGV[1]

FileUtils::mkdir_p outputDir
fileNames = Dir.entries(inputDir)

fileNames.each{ |fileName|

  if !fileName.end_with? ".txt" then
    puts "skip: " + fileName
    next
  end

  # set input and output paths
  inFilePath = inputDir + "/" + fileName
  outFilePath = outputDir + "/" + fileName

  puts "segment: " + inFilePath
  segRefs = []
  inFile = File.open(inFilePath)
  inFile.each_line {|line|
    segRef = Anystyle.parse(line, :tags)
    segRefs.push segRef
  }

  # write segmented reference strings
  open(outFilePath, 'w') { |outFile|
    segRefs.each { |element| outFile.puts(element) }
  }

}
