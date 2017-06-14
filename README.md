# pdf-evaluation

## General

This repository contains code for executing different evaluation tasks regarding the extraction of reference information from scientific publications in the PDF format.

## Reference String Extraction


The reference string extraction evaluation code is located at [src/main/java/de/exciteproject/pdf_evaluation/refextract/](src/main/java/de/exciteproject/pdf_evaluation/refextract/). The main executable is [EvaluationExecutor](src/main/java/de/exciteproject/pdf_evaluation/refextract/eval/EvaluationExecutor).

### Parameters of EvaluationExecutor

Examples for possible parameter lists can be found in the [amsd2017](https://github.com/exciteproject/amsd2017) repository in the `training-arguments.txt` files in some of the individual evaluation folders.

#### General parameters

* `args[0]`: Integer to select the execution mode:
    * 1: GROBID using trained models
    * 2: GROBID using the default models
    * 3: CERMINE using trained models
    * 4: CERMINE using the default models
    * 5: RefExt using trained models
    * 6: ParsCit using the specified executable file (see below)
* `args[1]`: Boolean to specify if new models should be trained
    * false: use existing models
    * true: train new models
* `args[2]`: Integer to specify the number of folds for cross-validation.
* `args[3]`: File that contains one file name (excluding file extensions) per line.
    * This list is assumed to be in a random order
    * From this, the k folds are created
    * This can be created with [RandomIdListBuilder](src/main/java/de/exciteproject/pdf_evaluation/list/RandomIdListBuilder).
* `args[4]`: Directory containing annotated files that are used for training the models
    * See below for details on the folder structure for the individual tools
* `args[5]`: Directory in which the fold directories will be created
    * Either the directory name does include a date and time in which case the existing folder will be used
    * Or the directory name does not include a date and time in which case a new folder with the current date and time will be created
* `args[6]`: Directory containing the pdf files which names correspond to the ids listed in the file given at `args[3]`
* `args]7]`: Directory containing files with annotated lines.
    * CERMINE, ParsCit, and Refext use the same files
    * GROBID have their own files due to differences in the PDF-to-text conversion
* `args[8]`: Integer specifying the evaluation mode
    * 0: evaluate on a line level
    * 1: evaluate on a reference string level by merging individual lines into reference strings

#### Tool-specific parameters

* GROBID using trained models (`args[0]=1`)
    * `args[9]`: Grobid Home directory
* GROBID using the default models (`args[0]=2`)
    * `args[9]`: Grobid Home directory
    * `args[10]`: directory containing the default grobid models
* RefExt using trained models (`args[0]=5`)
    * `args[9]`: Comma-separated list of features
    * `args[10]`: Comma-separated list of conjunctions
    * `args[11]`: Double specifying the trainer weight
    * `args[12]`: addStates name
        * one of `ThreeQuarterLabels`,`BiLabels`,`Labels`,`HalfLabels` as defined in Mallet
    * `args[13]`: Trainer name
        * one of `ByLabelLikelihood`,`ByL1LabelLikelihood`
    * `args[14]`: Comma-Separated list of target label replacements (Optional)
        * for example, `O-REF,O` replaces `O-REF` with `O` pprior training
* ParsCit (`args[0]=6`)
    * `args[9]`: Path to executable `citeExtract.pl` file
        
### Expected Folder Structures

`EvaluationExecutor` expects certain folder structures depending on the execution mode.



