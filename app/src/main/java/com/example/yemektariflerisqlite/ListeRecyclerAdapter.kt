package com.example.yemektariflerisqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.yemektariflerisqlite.databinding.RecyclerRowBinding


class ListeRecyclerAdapter(var yemekListesi : ArrayList<String>, val idListesi : ArrayList<Int>): RecyclerView.Adapter<ListeRecyclerAdapter.YemekHolder>(){


    class YemekHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val binding = RecyclerRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent,false )

        return YemekHolder(view)


    }

    override fun getItemCount(): Int {
        return yemekListesi.size

    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {
        holder.binding.recyclerRowText.text = yemekListesi[position]
        holder.itemView.setOnClickListener{
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("recyclerdangeldim",idListesi[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

}