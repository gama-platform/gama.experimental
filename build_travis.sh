#!/bin/bash
git_push(){
    echo "git push new change of parent pom"
    git config --global user.email "hqnghi88@gmail.com"
    git config --global user.name "hqnghi88"
    git config --global push.default simple
    git remote rm origin
    git remote add origin https://hqnghi88:$HQN_KEY@github.com/gama-platform/gama.experimental.git
    git add -A
    git commit -m "[ci skip] Generate parent and p2 pom"
    git push origin HEAD:master
}
generate_parent_pom(){
    header=$(<msi.gama.experimental.parent/pom_header.xml)
    current_modules=$(<msi.gama.experimental.parent/pom_modules.xml)
    footer=$(<msi.gama.experimental.parent/pom_footer.xml)

    modules=$'\n'$"<modules>"$'\n'
    for file in *; do 
      if [[ -d "$file" && ! -L "$file" ]]; then
        echo "$file is a directory"; 
         if [[ -f "$file/pom.xml" && "$file" != "msi.gama.experimental.parent" ]]; then
            echo "File $file/pom.xml found!"        
            modules="$modules <module>../$file</module> "$'\n'
        fi
      fi; 
    done

    modules="$modules </modules>"$'\n'
    if [[ "$current_modules" != "$modules" ]]; then
        echo "$modules" > msi.gama.experimental.parent/pom_modules.xml
        echo "$header $modules $footer"> msi.gama.experimental.parent/pom.xml
    fi
}
generate_p2updatesite_category(){
    header=$(<msi.gama.experimental.p2updatesite/category_header.xml)
    current_cate=$(<msi.gama.experimental.p2updatesite/category_body.xml)
    footer=$(<msi.gama.experimental.p2updatesite/category_footer.xml)

    cate=$'\n'$" "$'\n'
    for file in *; do 
      if [[ -d "$file" && ! -L "$file" ]]; then
         if [[ -f "$file/pom.xml" && ${file} != *"msi.gama.experimental.parent"* ]]; then
            
            if [[ ${file} == *"feature"* ]]; then	
               
                version=$(sed '/<parent>/,/<\/parent>/d;/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml")

                q=$".qualifier"
                version=${version/-SNAPSHOT/$q}

                cate="$cate <feature  url=\"features/"$file"_$version.jar\" id=\"$file\" version=\"$version\"> <category name=\"gama.optional\"/>   </feature>"$'\n'        
				cate=$(echo $cate|tr -d '\n')
                echo $cate
				echo 
                
            fi
        fi
      fi; 
    done

    echo $cate
    
    if [[ "$current_cate" != "$cate" ]]; then
        echo "$cate" > msi.gama.experimental.p2updatesite/category_body.xml
        echo "$header $cate $footer " > msi.gama.experimental.p2updatesite/category.xml
    fi
}

generate_parent_pom
generate_p2updatesite_category
#git_push

#cd msi.gama.experimental.parent &&
#mvn clean install -U &&
#cd -
